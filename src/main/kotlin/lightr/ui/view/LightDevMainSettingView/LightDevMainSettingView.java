package lightr.ui.view.LightDevMainSettingView;

import com.intellij.DynamicBundle;
import com.intellij.openapi.options.Configurable;
import lightr.config.GlobalState;
import lightr.interfaces.impl.GlobalStateService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * @author Lightr
 * @date 2025/6/17
 * @Description 描述
 */
public class LightDevMainSettingView implements Configurable {
    private final GlobalState state = GlobalStateService.getInstance().getState();
    private JTextField authorField;

    @Override
    public String getDisplayName() {
        return DynamicBundle.getBundle("locale.i18n").getString("dyg");
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Main panel with GridBagLayout to control overall structure
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Panel for the author row, using FlowLayout for simple left-to-right alignment
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // hgap=5, vgap=0
        ResourceBundle bundle = DynamicBundle.getBundle("locale.i18n");
        authorPanel.add(new JLabel(bundle.getString("author.label")));
        authorField = new JTextField(30);
        authorPanel.add(authorField);

        // Add the author panel to the main panel, anchored to the top-left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(authorPanel, gbc);

        // Add a vertical filler to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(new JPanel(), gbc);

        // Load initial state
        authorField.setText(state.getAuthor());

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !authorField.getText().equals(state.getAuthor());
    }

    @Override
    public void apply() {
        state.setAuthor(authorField.getText());
    }

    @Override
    public void reset() {
        authorField.setText(state.getAuthor());
    }
}
