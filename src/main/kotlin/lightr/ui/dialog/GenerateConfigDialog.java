package lightr.ui.dialog;

import com.intellij.database.model.DasNamed;
import com.intellij.database.model.DasTable;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ArrayUtil;
import freemarker.core.StopException;
import kotlin.Unit;
import lightr.config.ScopeState;
import lightr.data.GenerateContext;
import lightr.data.ScoredMember;
import lightr.data.TemplatePath;
import lightr.data.TemplateContextWrapper;
import lightr.data.table.TableData;
import lightr.interfaces.IHistorySelectedDelegate;
import lightr.interfaces.impl.GlobalStateService;
import lightr.interfaces.impl.HistoryStateService;
import lightr.ui.components.ListCheckboxComponent;
import lightr.ui.components.SelectionHistoryComboBox;
import lightr.ui.layout.DoubleColumnLayout;
import lightr.ui.layout.SingleColumnLayout;
import lightr.util.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateConfigDialog extends DialogWrapper {
    private final ScopeState scopeState;
    private final Project project;
    private final AnActionEvent event;
    private JPanel contentPane;
    private JLabel templateGroupLabel;
    private JLabel typeMappingLabel;
    private JLabel pathLabel;
    private JTextField pathInput;
    private JButton chooseButton;
    private JComboBox<String> templateGroupSelected;
    private JComboBox<String> typeMappingSelected;
    private JScrollPane tableScrollPanel;
    private JScrollPane generateTemplatePanel;
    private JButton templateChoose;
    private JButton refreshButton;
    private JButton selectAllButton;
    private JButton clearSelectAllButton;
    private JButton tableSelectButton;

    // private JButton savePathButton;
    private JTextField namespaceTextField;
    private JCheckBox namespaceLockCheckBox;

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        AtomicBoolean hasCloseDialog = new AtomicBoolean(true);
        try {
            if (scopeState.getSelectedTables().isEmpty()) {
                Messages.showErrorDialog("表是必需的", "错误");
                return;
            }

            var currentGlobalState = GlobalStateService.getInstance().getState();

            var fileNameMapTemplate = new HashMap<Path, String>();
            for (var path : scopeState.getSelectedTemplatePath()) {
                var fileContent = Files.readString(path);
                if (fileContent.length() > TemplateUtil.SPLIT_TAG.length() + 1) {
                    fileNameMapTemplate.put(path, fileContent);
                }
            }

            var mapperTemplates = currentGlobalState.getTypeMappingGroupMap().getOrDefault(scopeState.getSelectTypeMapping(), Set.of()).stream().sorted().toList();
            if (mapperTemplates.isEmpty()) {
                Messages.showErrorDialog("TypeMapper is empty", "Error");
                return;
            }

            // build context
            var datasource = DbUtil.getDatasource(DbUtil.getAllDatasource(project), scopeState.getSelectedTables().stream().findFirst().orElseThrow());

            var mapperUtil = new MapperUtil(mapperTemplates);
            var dbContext = new GenerateContext(mapperTemplates, datasource);

            var fullSelectTables = scopeState.getSelectedTables().stream().map(x -> new TableData(x, dbContext)).toList();
            var templateSharedContext = new TemplateContextWrapper();

            // process template
            scopeState.getSelectedTables().stream().map(x -> new TableData(x, dbContext)).forEach(tableData -> {

                fileNameMapTemplate.entrySet().stream().sorted((Map.Entry.comparingByKey())).forEachOrdered(entry -> {
                    var templatePath = entry.getKey().toString().replace(StringUtil.defaultIfEmpty(scopeState.getTemplateGroupPath(), ""), "");

                    try {
                        // region process template context
                        var context = new HashMap<String, Object>();
                        context.put("author", GlobalStateService.getInstance().getState().getAuthor());
                        context.put("context", templateSharedContext);
                        context.put("namespace", namespaceTextField.getText());
                        context.put("dbms", datasource.getDbms());
                        context.put("selectedTables", fullSelectTables);
                        context.put("table", tableData);
                        context.put("columns", tableData.getColumns());
                        context.put("NameUtil", NameUtil.INSTANCE);
                        context.put("MapperUtil", mapperUtil);
                        context.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        context.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        String sourceCode;
                        try (var bo = new ByteArrayOutputStream()) {
                            TemplateUtil.evaluate(context, new OutputStreamWriter(bo, StandardCharsets.UTF_8), templatePath, entry.getValue());
                            sourceCode = bo.toString(StandardCharsets.UTF_8);
                        }

                        var extracted = TemplateUtil.extractConfig(TemplateUtil.SPLIT_TAG_REGEX, sourceCode);
                        var templateConfig = extracted.component1();
                        sourceCode = extracted.component2();

                        // endregion

                        var fileName = StringUtil.isEmpty(templateConfig.getFileName())
                                ? entry.getKey().getFileName().toString() : templateConfig.getFileName();

                        var templateName = StaticUtil.extractDirAfterName(entry.getKey().toString(), String.valueOf(scopeState.getTemplateGroupPath()));
                        var customPath = scopeState.getTemplateCustomPath(templateName);

                        String basePath = scopeState.getPath();
                        String relativePath;

                        if (customPath != null) {
                            relativePath = customPath;
                        } else if (StringUtil.isEmpty(templateConfig.getDir())) {
                            relativePath = StaticUtil.extractDirAfterName(
                                templatePath.substring(0, templatePath.lastIndexOf(FileSystems.getDefault().getSeparator()) + 1),
                                String.valueOf(scopeState.getTemplateGroupPath())
                            );
                        } else {
                            relativePath = templateConfig.getDir();
                        }

                        // 规范化路径分隔符
                        relativePath = relativePath.replace('\\', '/');
                        // 确保相对路径不以斜杠开头
                        relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
                        // 确保相对路径以斜杠结尾
                        relativePath = relativePath.endsWith("/") ? relativePath : relativePath + "/";

                        var outPath = Path.of(basePath).resolve(relativePath).resolve(fileName).normalize();

                        var file = outPath.toFile();
                        if (!file.exists()) {
                            var ignore = file.getParentFile().mkdirs();

                            if (!file.createNewFile()) {
                                StaticUtil.showWarningNotification("Create File", "Can not create file", project, NotificationType.WARNING);
                                return;
                            }
                        }

                        try (var writer = new OutputStreamWriter(new FileOutputStream(outPath.toString()), StandardCharsets.UTF_8)) {
                            writer.write(sourceCode);
                            writer.flush();
                        }

                        StaticUtil.showWarningNotification("LightDev", templatePath + " 生成成功", project, NotificationType.INFORMATION);

                    } catch (StopException _ignore) {
//                        StaticUtil.showWarningNotification("Template Generate", entry.getKey() + " Stop process", project, NotificationType.WARNING);
                    } catch (Exception e) {
                        Messages.showErrorDialog("Template: " + templatePath + "\n" + e.getMessage() + "\n" + StaticUtil.extractFullStack(e), "Error");
                        hasCloseDialog.set(false);
                    }
                });
            });

            if (hasCloseDialog.get()) {
                super.doOKAction();
            }

        } catch (Exception e) {

            Messages.showErrorDialog(e.getMessage() + "\n" + StaticUtil.extractFullStack(e), "Error");
        }
    }

    private void refreshTemplateGroupSelect() {
        templateGroupSelected.removeAllItems();

        var globalHistoryState = HistoryStateService.getInstance().getState();

        globalHistoryState.getHistoryUsePath().stream().sorted().forEachOrdered(input -> templateGroupSelected.insertItemAt(input.getMember(), 0));

        if (templateGroupSelected.getItemCount() > 0) {
            templateGroupSelected.setSelectedIndex(0);
        }
    }

    /**
     * 选择要生成的模板
     */
    private void refreshGenerateTemplatePanel() {
        var templatePath = scopeState.getTemplateGroupPath();

        if (templatePath == null) {
            return;
        }

        var globalHistoryState = HistoryStateService.getInstance().getState();
        var dir = Path.of(templatePath);
        var file = dir.toFile();
        if (!file.exists()) {
            globalHistoryState.getHistoryUsePath().remove(new ScoredMember(templatePath));
            return;
        }
        var templateFiles = FileUtil.fileTraverser(file).toList();

        if (!templatePath.isBlank()) {
            globalHistoryState.getHistoryUsePath().add(new ScoredMember(templatePath));
        }

        var paths = new HashMap<String, Path>();

        var fileNames = templateFiles.stream().filter(File::isFile)
                .peek(x -> paths.put(StaticUtil.extractDirAfterName(x.getPath(), dir.toString()), x.toPath()))
                .map(x -> StaticUtil.extractDirAfterName(x.getPath(), dir.toString()))
                .collect(Collectors.toList());

        // 获取保存的模板路径
        var currentGlobalState = GlobalStateService.getInstance().getState();
        var savedPaths = currentGlobalState.getTemplatePaths();

        var tables = new ListCheckboxComponent(
            new DoubleColumnLayout(),
            fileNames,
            true,
            (templateName, customPath) -> {
                scopeState.setTemplateCustomPath(templateName, customPath);
                // 保存模板路径
                var newTemplatePath = new TemplatePath(templateName, customPath);
                currentGlobalState.getTemplatePaths().remove(newTemplatePath);
                if (customPath != null && !customPath.isBlank()) {
                    currentGlobalState.getTemplatePaths().add(newTemplatePath);
                }
                GlobalStateService.getInstance().loadState(currentGlobalState);
                return Unit.INSTANCE;
            }
        );

        // 设置已保存的路径
        for (var savedPath : new ArrayList<>(savedPaths)) {
            if (savedPath.getTemplateName() != null && savedPath.getPath() != null) {
                scopeState.setTemplateCustomPath(savedPath.getTemplateName(), savedPath.getPath());
                // 在界面上显示已保存的路径
                var componentIndex = tables.getCheckBoxList().stream()
                        .filter(x -> x.getText().equals(savedPath.getTemplateName()))
                        .findFirst()
                        .map(x -> ((Container) tables).getComponentZOrder(x) + 1)
                        .orElse(-1);
                if (componentIndex != -1) {
                    var component = ((Container) tables).getComponent(componentIndex);
                    if (component instanceof JBTextField) {
                        ((JBTextField) component).setText(savedPath.getPath());
                    }
                }
            }
        }

        for (var actionListener : selectAllButton.getActionListeners()) {
            selectAllButton.removeActionListener(actionListener);
        }
        selectAllButton.addActionListener(e -> Objects.requireNonNull(tables.getCheckBoxList()).forEach(x -> x.setSelected(true)));

        for (var actionListener : clearSelectAllButton.getActionListeners()) {
            clearSelectAllButton.removeActionListener(actionListener);
        }
        clearSelectAllButton.addActionListener(e -> Objects.requireNonNull(tables.getCheckBoxList()).forEach(x -> x.setSelected(false)));
        // 添加保存路径按钮
        // savePathButton = new JButton("保存路径配置");
        // savePathButton.addActionListener(e -> {
        //     var customPaths = scopeState.getAllTemplateCustomPaths();
        //     currentGlobalState.getTemplatePaths().clear();
        //     for (var entry : customPaths.entrySet()) {
        //         if (entry.getValue() != null && !entry.getValue().isBlank()) {
        //             currentGlobalState.getTemplatePaths().add(new TemplatePath(entry.getKey(), entry.getValue()));
        //         }
        //     }
        //     GlobalStateService.getInstance().loadState(currentGlobalState);
        //     StaticUtil.showWarningNotification("保存路径配置", "模板路径配置已保存", project, NotificationType.INFORMATION);
        // });

        // The ListCheckboxComponent now implements Scrollable, so it can be set as the viewport view directly.
        // This allows the scroll pane to correctly manage the component's width.
        generateTemplatePanel.setViewportView(tables);

        scopeState.setTemplateFilePath(paths, tables);
//            Objects.requireNonNull(scopeState.getTemplateGroup()).setSelectedItem(templatePath);

    }

    private void initDatabaseTreeState() {
        //获取选中的所有表
        // 获取当前项目所有数据源
        var allTables = new HashMap<String, DasTable>(16);

        var dbTables = DasUtil.extractTableFromDatabase(event.getDataContext());
        var selectTables = DasUtil.extractSelectTablesFromPsiElement(event.getDataContext()).map(DasNamed::getName).collect(Collectors.toSet());

        dbTables.forEach(x -> allTables.put(x.getName(), x));

        if (allTables.isEmpty()) {
            return;
        }

        var model = new DefaultListModel<String>();
        for (var table : allTables.keySet()) {
            model.addElement(table);
        }

        var tables = new ListCheckboxComponent(
            new SingleColumnLayout(),
            allTables.values().stream().sorted((a, b) -> {
                if (a.getDasParent() == null) {
                    return 1;
                }
                if (b.getDasParent() == null) {
                    return -1;
                }
                var i = b.getDasParent().getName().compareTo(a.getDasParent().getName());
                return i == 0 ? b.getName().compareTo(a.getName()) : i;
            }).map(DasNamed::getName).toList(),
            false,
            null
        );

        Objects.requireNonNull(tables.getCheckBoxList()).forEach(x -> {
            if (selectTables.contains(x.getText())) {
                x.setSelected(true);
            }
        });
        if (tables.getCheckBoxList().stream().anyMatch(jbCheckBox -> !jbCheckBox.isSelected())) {
            tableSelectButton.setText("选择全部");
        } else {
            tableSelectButton.setText("清除全部");
        }

        tableSelectButton.addActionListener(e -> {
            if (tables.getCheckBoxList().stream().anyMatch(jbCheckBox -> !jbCheckBox.isSelected())) {
                tableSelectButton.setText("清除全部");
                tables.getCheckBoxList().forEach(x -> x.setSelected(true));
            } else {
                tableSelectButton.setText("选择全部");
                tables.getCheckBoxList().forEach(x -> x.setSelected(false));
            }
        });

        tableScrollPanel.setViewportView(tables);

        scopeState.setAllTableAndComponent(allTables, tables);

    }

    private void createUIComponents() {
        templateGroupSelected = new SelectionHistoryComboBox(new IHistorySelectedDelegate<>() {

            @Override
            public void selectItem(String item) {
                var instance = HistoryStateService.getInstance();
                var state = instance.getState();
                for (var member : state.getHistoryUsePath()) {
                    if (member.getMember().equals(item)) {
                        member.setScore(System.currentTimeMillis());
                        instance.loadState(state);
                        break;
                    }
                }
            }

            @Override
            public @Nullable String getSelectItem() {
                var state = HistoryStateService.getInstance().getState();
                return state.getHistoryUsePath().stream()
                        .peek(x -> {
                            if (x.getScore() == null) x.setScore(0L);
                        })
                        .max(Comparator.comparingLong(ScoredMember::getScore))
                        .map(ScoredMember::getMember)
                        .orElse(null);
            }

            @Override
            public @NotNull Collection<String> getSelectList() {
                var state = HistoryStateService.getInstance().getState();
                return state.getHistoryUsePath().stream()
                        .filter(x -> x.getMember() != null)
                        .map(x -> String.valueOf(x.getMember()))
                        .toList();
            }
        });
        templateGroupSelected.setEditable(true);
        templateGroupSelected.addItemListener(e -> refreshGenerateTemplatePanel());
        typeMappingSelected = new SelectionHistoryComboBox(new IHistorySelectedDelegate<>() {
            @Override
            public void selectItem(String item) {
                var state = HistoryStateService.getInstance().getState();
                state.setHistoryUseTypeMapper(item);
            }

            @Override
            public @NotNull Collection<String> getSelectList() {
                var currentGlobalState = GlobalStateService.getInstance().getState();
                return currentGlobalState.getTypeMappingGroupMap().keySet();
            }

            @Override
            public @Nullable String getSelectItem() {
                var state = HistoryStateService.getInstance().getState();
                return state.getHistoryUseTypeMapper();
            }
        });
    }

    public GenerateConfigDialog(AnActionEvent event) {
        super(event.getProject());

        this.event = event;
        this.project = event.getProject();
        scopeState = new ScopeState(project, pathInput, templateGroupSelected, typeMappingSelected);
        setModal(true);

        // 设置窗口自适应大小
        pack();
        // 设置最小窗口大小
        getWindow().setMinimumSize(new Dimension(600, 400));

        Function<ActionEvent, Optional<VirtualFile>> fileChooserConsumer = e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            descriptor.setForcedToUseIdeaFileChooser(true);
            descriptor.setTitle("选择路径");
            // 2. 弹出路径选择器
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
            return Optional.ofNullable(virtualFile);
        };
        chooseButton.addActionListener(e -> fileChooserConsumer.apply(e).ifPresent(x -> scopeState.setGenerateFileStorePath(x.getPath())));

        templateChoose.addActionListener(e -> fileChooserConsumer.apply(e).ifPresent(x -> scopeState.setTemplateGroupPath(x.getPath())));
        pathInput.setText(Objects.requireNonNull(project).getBasePath());
        namespaceTextField.setText(project.getName());
        namespaceTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                namespaceLockCheckBox.setSelected(true);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        pathInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNamespaceTextInput();
            }

            private void updateNamespaceTextInput() {
                if (namespaceLockCheckBox.isSelected()) {
                    return;
                }

                var beginIndex = pathInput.getText().indexOf(project.getName());
                if (beginIndex == -1) {
                    namespaceTextField.setText(pathInput.getText());
                    return;
                } else {
                    beginIndex = beginIndex + project.getName().length();
                }

                var text = project.getName() + pathInput.getText().substring(beginIndex);
                if (text.isBlank()) {
                    namespaceTextField.setText(project.getName());
                } else {
                    namespaceTextField.setText(text.replaceAll("[/|\\\\]", "."));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNamespaceTextInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        refreshTemplateGroupSelect();

        initDatabaseTreeState();

        refreshButton.addActionListener(e -> refreshGenerateTemplatePanel());

        init();
    }
}
