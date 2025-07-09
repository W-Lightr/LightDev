package lightr.config

import com.intellij.database.model.DasTable
import com.intellij.openapi.project.Project
import lightr.ui.components.ListCheckboxComponent
import java.nio.file.Path
import javax.swing.JComboBox
import javax.swing.JTextField

class ScopeState(
    val project: Project?,
    /**
     * 输出文件的路径
     */
    private val pathInput: JTextField?,
    /**
     * 模板文件的路径
     * 支持历史选择
     */
    private val templateGroup: JComboBox<String>?,
    /**
     * 文本映射信息
     */
    private val typeMappingSelected: JComboBox<String>) {
    /**
     * 数据源中所有的表
     */
    private var allTables: Map<String, DasTable>? = null

    /**
     * 用来选择生成哪些表的组件
     */
    private var selectTableComponent: ListCheckboxComponent? = null

    var templateGroupPath: String?
        get() = templateGroup?.selectedItem.toString()
        set(template) {
            templateGroup?.addItem(template)
            templateGroup?.selectedItem = template
        }

    val path: String
        get() = pathInput!!.text

    fun setGenerateFileStorePath(path: String) {
        pathInput!!.text = path
    }

    fun getSelectTypeMapping(): String? {
        return typeMappingSelected.selectedItem?.toString()
    }
    fun setAllTableAndComponent(allTables: Map<String, DasTable>, component: ListCheckboxComponent) {

        this.allTables = allTables
        this.selectTableComponent = component
    }

    fun getSelectedTables(): Set<DasTable> {
        val selectedItems = selectTableComponent?.selectedItems

        return selectedItems?.mapNotNull {
            allTables?.get(it)
        }?.toSet() ?: setOf()
    }

    /**
     * 显示模板文件的名称和模板的路径
     * @key 展示在模板中的名称
     * @value 模板的路径
     */
    private var templateFilePath: Map<String, Path>? = null

    /**
     * 每个模板文件的自定义生成路径
     */
    private var templateCustomPaths: MutableMap<String, String> = mutableMapOf()

    /**
     * 获取所有模板的自定义路径
     */
    fun getAllTemplateCustomPaths(): Map<String, String> {
        return templateCustomPaths.toMap()
    }

    /**
     * 选择模板文件的组件
     */
    private var selectTemplateComponent: ListCheckboxComponent? = null

    fun setTemplateFilePath(paths: Map<String, Path>, component: ListCheckboxComponent) {
        templateFilePath = paths
        selectTemplateComponent = component
    }

    fun getSelectedTemplatePath(): Set<Path> {
        val selectedItems = selectTemplateComponent?.selectedItems

        return selectedItems?.mapNotNull {
            templateFilePath?.get(it)
        }?.toSet() ?: setOf()
    }

    /**
     * 设置模板的自定义生成路径
     */
    fun setTemplateCustomPath(templateName: String, customPath: String) {
        templateCustomPaths[templateName] = customPath
    }

    /**
     * 获取模板的自定义生成路径
     */
    fun getTemplateCustomPath(templateName: String): String? {
        return templateCustomPaths[templateName]
    }

    /**
     * 清除模板的自定义生成路径
     */
    fun clearTemplateCustomPath(templateName: String) {
        templateCustomPaths.remove(templateName)
    }
}
