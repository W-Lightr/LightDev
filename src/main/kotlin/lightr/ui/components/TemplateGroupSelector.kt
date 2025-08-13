package lightr.ui.components

import com.intellij.database.util.common.isNotNullOrEmpty
import com.intellij.openapi.ui.Messages
import javax.swing.JButton
import lightr.interfaces.IHistorySelectedDelegate
import lightr.interfaces.impl.HistoryStateService
import lightr.data.ScoredMember
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * 模板组选择器，支持删除功能
 * 内置模板不能删除，外置模板可以删除
 */
class TemplateGroupSelector(private val delegate: IHistorySelectedDelegate<String>) : JPanel(BorderLayout()) {
    
    private val comboBox: JComboBox<String> = JComboBox()
    private val deleteButton: JButton = JButton("删除")
    private var onDeleteCallback: (() -> Unit)? = null
    
    init {
        setupComponents()
        refreshItems()
        initLastSelectedItem()
        setupListeners()
    }
    
    private fun setupComponents() {
        // 设置下拉列表
        comboBox.isEditable = true
        
        // 设置删除按钮
        deleteButton.toolTipText = "删除选中的模板路径（内置模板不可删除）"
        deleteButton.preferredSize = java.awt.Dimension(60, deleteButton.preferredSize.height)
        
        // 布局
        add(comboBox, BorderLayout.CENTER)
        add(deleteButton, BorderLayout.EAST)
    }
    
    private fun refreshItems() {
        comboBox.removeAllItems()
        delegate.getSelectList().forEach { comboBox.addItem(it) }
    }
    
    private fun initLastSelectedItem() {
        val lastSelectedItem = delegate.getSelectItem()
        if (lastSelectedItem.isNotNullOrEmpty) {
            for (i in 0 until comboBox.itemCount) {
                if (comboBox.getItemAt(i) == lastSelectedItem) {
                    comboBox.selectedItem = lastSelectedItem
                    break
                }
            }
        }
    }
    
    private fun setupListeners() {
        // 下拉列表选择监听
        comboBox.addItemListener {
            val toString = it.item.toString()
            if (toString.isEmpty()) {
                return@addItemListener
            }
            delegate.selectItem(toString)
            updateDeleteButtonState()
        }
        
        // 删除按钮监听
        deleteButton.addActionListener {
            deleteSelectedTemplate()
        }
        
        // 初始化删除按钮状态
        updateDeleteButtonState()
    }
    
    private fun updateDeleteButtonState() {
        val selectedItem = comboBox.selectedItem?.toString()
        // 内置模板不能删除（以"内置："开头的模板）
        val isBuiltinTemplate = selectedItem?.startsWith("内置：") == true
        deleteButton.isEnabled = !selectedItem.isNullOrEmpty() && !isBuiltinTemplate
    }
    
    private fun deleteSelectedTemplate() {
        val selectedItem = comboBox.selectedItem?.toString()
        if (selectedItem.isNullOrEmpty()) {
            return
        }
        
        // 检查是否是内置模板
        if (selectedItem.startsWith("内置：")) {
            Messages.showWarningDialog(
                "内置模板不能删除",
                "删除模板"
            )
            return
        }
        
        // 确认删除
        val result = Messages.showYesNoDialog(
            "确定要删除模板路径 \"$selectedItem\" 吗？\n删除后将从历史记录中移除。",
            "删除模板",
            Messages.getQuestionIcon()
        )
        
        if (result == Messages.YES) {
            // 从历史记录中删除
            val historyState = HistoryStateService.getInstance().getState()
            val memberToRemove = ScoredMember(selectedItem)
            historyState.historyUsePath.remove(memberToRemove)
            HistoryStateService.getInstance().loadState(historyState)
            
            // 刷新下拉列表
            refreshItems()
            
            // 选择第一个可用项
            if (comboBox.itemCount > 0) {
                comboBox.selectedIndex = 0
            }
            
            updateDeleteButtonState()
            
            // 调用删除回调
            onDeleteCallback?.invoke()
            
            Messages.showInfoMessage(
                "模板路径已删除",
                "删除模板"
            )
        }
    }
    
    // 提供与JComboBox兼容的方法
    fun addItemListener(listener: java.awt.event.ItemListener) {
        comboBox.addItemListener(listener)
    }
    
    fun removeAllItems() {
        comboBox.removeAllItems()
    }
    
    fun insertItemAt(item: String, index: Int) {
        comboBox.insertItemAt(item, index)
    }
    
    fun getItemCount(): Int {
        return comboBox.itemCount
    }
    
    fun setSelectedIndex(index: Int) {
        comboBox.selectedIndex = index
    }
    
    fun getSelectedItem(): Any? {
        return comboBox.selectedItem
    }
    
    fun setSelectedItem(item: Any?) {
        comboBox.selectedItem = item
    }
    
    fun addItem(item: String) {
        comboBox.addItem(item)
    }
    
    fun setEditable(editable: Boolean) {
        comboBox.isEditable = editable
    }
    
    // 获取内部的JComboBox，用于兼容现有代码
    fun getComboBox(): JComboBox<String> {
        return comboBox
    }
    
    // 设置删除回调函数
    fun setOnDeleteCallback(callback: () -> Unit) {
        onDeleteCallback = callback
    }
}