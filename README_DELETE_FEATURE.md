# 模板文件夹删除功能实现说明

## 功能概述
为模板文件夹的下拉列表添加了删除功能，支持删除外置模板，但内置模板不能删除。

## 实现方案

### 1. 新增组件 TemplateGroupSelector
- 位置：`src/main/kotlin/lightr/ui/components/TemplateGroupSelector.kt`
- 功能：替代原来的 `SelectionHistoryComboBox`，增加删除按钮
- 特性：
  - 包含下拉列表和删除按钮的复合组件
  - 自动识别内置模板（以"内置："开头），禁用删除按钮
  - 删除前显示确认对话框
  - 删除后自动刷新列表和选择第一个可用项
  - 支持删除回调函数

### 2. 修改 GenerateConfigDialog
- 位置：`src/main/kotlin/lightr/ui/dialog/GenerateConfigDialog.java`
- 变更：
  - 将 `templateGroupSelected` 类型从 `JComboBox<String>` 改为 `TemplateGroupSelector`
  - 在 `createUIComponents` 方法中使用新组件
  - 设置删除回调，删除后刷新模板组选择和模板面板
  - 通过 `getComboBox()` 方法保持与 `ScopeState` 的兼容性

### 3. 更新UI表单
- 位置：`src/main/kotlin/lightr/ui/dialog/GenerateConfigDialog.form`
- 变更：将组件类型从 `javax.swing.JComboBox` 改为 `lightr.ui.components.TemplateGroupSelector`

## 使用说明

1. **查看模板列表**：打开代码生成对话框，可以看到模板组下拉列表旁边有一个"删除"按钮

2. **删除外置模板**：
   - 选择一个外置模板（不以"内置："开头的模板）
   - 点击"删除"按钮
   - 确认删除操作
   - 模板将从历史记录中移除

3. **内置模板保护**：
   - 选择内置模板时，删除按钮会自动禁用
   - 尝试删除内置模板会显示警告信息

## 技术细节

### 删除逻辑
1. 检查选中的模板是否为内置模板
2. 显示确认对话框
3. 从 `HistoryStateService` 的 `historyUsePath` 中移除对应的 `ScoredMember`
4. 刷新下拉列表
5. 选择第一个可用项
6. 触发回调函数刷新相关UI

### 兼容性保证
- `TemplateGroupSelector` 提供了与 `JComboBox` 兼容的方法
- 通过 `getComboBox()` 方法返回内部的 `JComboBox` 实例
- 保持了与现有 `ScopeState` 和其他组件的兼容性

## 安全性
- 内置模板受到保护，无法被删除
- 删除操作需要用户确认
- 删除后会显示成功提示

这个实现既满足了用户需求，又保证了系统的稳定性和安全性。