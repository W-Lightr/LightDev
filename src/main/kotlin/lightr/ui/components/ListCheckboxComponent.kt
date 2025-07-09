package lightr.ui.components

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import lightr.interfaces.ILayoutDelegate
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * 列表复选框组件
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 17:12
 */
class ListCheckboxComponent(
    layoutDelegate: ILayoutDelegate,
    /**
     * 原属列表
     */
    private val items: Collection<String>,
    /**
     * 是否显示路径输入框
     */
    private val showPathInput: Boolean = false,
    /**
     * 路径变更回调
     */
    private val onPathChange: ((String, String) -> Unit)? = null
) :
    JPanel(GridBagLayout()) {
    /**
     * 复选框列表
     */
    var checkBoxList: Collection<AbstractButton>? = null
        private set

    /**
     * 路径输入框列表
     */
    private val pathInputs = mutableMapOf<String, JBTextField>()

    init {
        this.initCheckBox(layoutDelegate)
    }

    /**
     * 初始化操作
     */
    private fun initCheckBox(layoutDelegate: ILayoutDelegate) {
        if (items.isEmpty()) {
            return
        }

        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        var row = 0
        checkBoxList = items.map { item ->
            val checkBox = JBCheckBox(item)
            
            gbc.gridx = 0
            gbc.gridy = row
            gbc.weightx = 0.3
            add(checkBox, gbc)

            if (showPathInput) {
                val pathInput = JBTextField().apply {
                    toolTipText = "输入生成路径"
                    addCaretListener { 
                        onPathChange?.invoke(item, text)
                    }
                }
                pathInputs[item] = pathInput

                val chooseButton = JButton("...").apply {
                    toolTipText = "选择路径"
                    addActionListener {
                        val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
                        val chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, null, this@ListCheckboxComponent)
                        val file = chooser.choose(null).firstOrNull()
                        if (file != null) {
                            pathInput.text = file.path
                            onPathChange?.invoke(item, file.path)
                        }
                    }
                }

                gbc.gridx = 1
                gbc.weightx = 0.6
                add(pathInput, gbc)

                gbc.gridx = 2
                gbc.weightx = 0.1
                add(chooseButton, gbc)
            }

            row++
            checkBox
        }
    }

    val selectedItems: List<String>
        /**
         * 获取已选中的元素
         *
         * @return 已选中的元素
         */
        get() {
            if (checkBoxList == null || checkBoxList!!.isEmpty()) {
                return emptyList()
            }
            val result: MutableList<String> = ArrayList()
            checkBoxList!!.forEach { checkBox ->
                if (checkBox.isSelected) {
                    result.add(checkBox.text)
                }
            }
            return result
        }
}
