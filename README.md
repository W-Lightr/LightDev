# LightDev

![Plugin Version](https://img.shields.io/badge/version-0.0.1-blue)
![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2024.2.5-orange)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

一个强大的 IntelliJ IDEA 插件，专为提高开发效率而设计。LightDev 提供了智能的代码生成功能，特别是基于数据库表结构的模板代码生成，帮助开发者快速构建项目骨架。

## ✨ 主要功能

### 🚀 模板代码生成
- **数据库驱动**：基于数据库表结构自动生成代码
- **多模板支持**：内置 Mybatis-Plus 模板，支持自定义模板
- **智能映射**：自动处理数据库类型到 Java 类型的映射
- **批量生成**：支持同时为多个表生成代码

### ⚙️ 配置管理
- **全局设置**：配置作者信息等全局参数
- **类型映射**：自定义数据库类型到编程语言类型的映射
- **模板管理**：支持添加、删除自定义模板（内置模板受保护）
- **历史记录**：记录常用的模板和配置选择

### 🛠️ 高级特性
- **模板删除功能**：支持删除外部模板，内置模板自动保护
- **智能提示**：基于历史使用记录的智能建议
- **多语言支持**：支持中文和英文界面
- **实时预览**：生成前可预览代码结构

## 📦 安装

### 方式一：从 JetBrains 插件市场安装（推荐）
1. 打开 IntelliJ IDEA
2. 进入 `File` → `Settings` → `Plugins`
3. 搜索 "LightDev"
4. 点击 `Install` 安装
5. 重启 IDE

### 方式二：手动安装
1. 下载最新的插件包（.zip 文件）
2. 打开 IntelliJ IDEA
3. 进入 `File` → `Settings` → `Plugins`
4. 点击齿轮图标 → `Install Plugin from Disk...`
5. 选择下载的插件包
6. 重启 IDE

## 🚀 快速开始

### 1. 配置插件
1. 打开 `File` → `Settings` → `LightDev`
2. 在 "其他设置" 中配置作者信息
3. 在 "类型映射设置" 中根据需要调整类型映射

### 2. 生成代码
1. 在数据库工具窗口中选择数据库表
2. 右键点击表名
3. 选择 "生成代码（L）"
4. 在弹出的对话框中：
   - 选择模板组（如 Mybatis-Plus）
   - 配置包名和输出路径
   - 选择要生成的代码类型
   - 点击 "生成" 按钮

## 📝 模板语法示例

编写模板所需的一些变量和使用方法

### 变量说明

#### 1.1 NameUtil

提供方法:

- toCamelCase()  首字母小写，后续每个单词首字母大写
- toPascalCase()  每个单词的首字母都大写
- toSnakeCase()  所有字母小写，单词之间用下划线连接
- toKebabCase()  所有字母小写，单词之间用连字符连接
- toScreamingSnakeCase()  所有字母大写，单词之间用下划线连接

使用方式

```bash
# ftl模板
<#assign PascalTableName=NameUtil.toPascalCase(table.getRawName())>
<#assign SnakeCaseName=NameUtil.toSnakeCase(table.getRawName())>

class ${PascalTableName}Controller
```

#### 1.2 namespace

当前选择的的代码包

````bash
package ${namespace}.services
````

#### 1.3 table

这是表的相关信息变量

| 方法                | 描述         | 示例                                                         |
| ------------------- | ------------ | ------------------------------------------------------------ |
| getRawName()        | 表名         | table.getRawName()                                           |
| getRawComment()     | 表描述       | table.getRawComment()                                        |
| getRawStatement()   | 建表语句     | table.getRawStatement()                                      |
| getPrimaryColumns() | 索引字段列表 | table.getPrimaryColumns()?first \|\|  ${myList?first!"默认值"} \|\| ${list[0]}   \|\| <#if list?has_content> |

#### 1.4 columns

来源于table ,是一个list

使用遍历:

```bash
<#list columns as column>
    val ${NameUtil.toCamelCase(column.getRawName())}: ${column.getMapperType()}? = null, // ${column.getRawComment()}
</#list>
```

单个column的属性如下

| 方法                 | 描述             | 示例                                          |
| -------------------- | ---------------- | --------------------------------------------- |
| getMapperType()      | java的类型       | column.getMapperType() // 结果: string        |
| getRawType()         | 数据库字段类型   | column.getRawType()    // 结果：varchar(32)   |
| getRawName()         | 数据库字段名     | column.getRawName()   // 结果: id 、user_name |
| getRawComment()      | 数据库字段描述   | column.getRawComment()                        |
| getRawDefaultValue() | 数据库字段默认值 | column.getRawDefaultValue()                   |
| hasPrimaryKey()      | 是否是主键       | <#if column.hasPrimaryKey()>                  |
| hasForeignKey()      | 是否有外键       | <#if column.hasForeignKey()>                  |
| hasIndex()           | 是否有索引       | <#if column.hasIndex()>                       |



#### 1.5 文件名称

生成的文件名称需要在模板中定义格式

以`#region config`开始和`#endregion`结束的区域，参考下面的定义方式

> region config 的配置需要放在代码开头，切勿放在代码中

```bash
<#assign PascalTableName=NameUtil.toPascalCase(table.getRawName())>
<#assign CamelTableName=NameUtil.toCamelCase(table.getRawName())>

#region config
fileName=${PascalTableName}.java
#endregion

package ${namespace}.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
```



#### 1.6 其他变量

| 变量名   | 使用        | 备注                             |
| -------- | ----------- | -------------------------------- |
| author   | ${author}   | 作者信息，在设置中自定义作者名称 |
| date     | ${date}     | 当前时间（yyyy-MM-dd）           |
| dateTime | ${dateTime} | 当前时间（yyyy-MM-dd HH:mm:ss）  |



## 🎯 支持的数据库

- MySQL
- PostgreSQL
- Oracle
- SQL Server
- SQLite
- H2
- 其他 JDBC 兼容数据库

## 🔧 系统要求

- **IntelliJ IDEA**: 2024.2+ (Ultimate Edition 推荐)
- **Java**: JDK 21+
- **数据库插件**: 需要启用 IntelliJ 的 Database 插件

## 📚 使用技巧

### 模板管理
- 内置模板不可删除，确保系统稳定性
- 外部模板可以通过界面删除按钮移除
- 支持模板历史记录，方便快速选择常用模板

### 类型映射
- 可以根据项目需求自定义数据库类型映射
- 支持导入/导出映射配置，便于团队共享



## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🔗 相关链接

- [GitHub 仓库](https://github.com/W-Lightr/LightDev)
- [问题反馈](https://github.com/W-Lightr/LightDev/issues)
- [更新日志](CHANGELOG.md)

## 👨‍💻 作者

**Lightr** - [GitHub](https://github.com/W-Lightr)

---

如果这个插件对你有帮助，请给个 ⭐ Star 支持一下！