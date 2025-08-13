<#-- 1. 定义模板中需要使用的通用变量 -->
<#-- 根据表名生成符合Java规范的类名 (大驼峰) 和实例名 (小驼峰) -->
<#assign PascalTableName = NameUtil.toPascalCase(table.getRawName())>
<#assign CamelTableName = NameUtil.toCamelCase(table.getRawName())>

<#-- 2. 获取主键信息 (增强模板的通用性) -->
<#-- 判断表是否有主键 -->
<#if table.getPrimaryColumns()?has_content>
    <#-- 获取第一个主键列 -->
    <#assign pkColumn = table.getPrimaryColumns()?first>
    <#-- 获取主键的Java类型 (e.g., Long, String, Integer) -->
    <#assign pkType = pkColumn.getMapperType()>
    <#-- 获取主键的字段名，并转为小驼峰 (e.g., userId) -->
    <#assign pkName = NameUtil.toCamelCase(pkColumn.getRawName())>
<#else>
    <#-- 如果表没有主键，则使用默认的 "String id" 作为后备，以保证模板能生成 -->
    <#assign pkType = "String">
    <#assign pkName = "id">
</#if>

<#-- 3. 配置文件名，必须放在模板顶部 -->
#region config
fileName=${PascalTableName}Controller.java
#endregion

package ${namespace}.controller;

import ${namespace}.model.entity.${PascalTableName};
import ${namespace}.service.intf.${PascalTableName}Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * ${table.getRawComment()}(${table.getRawName()})表控制层
 *
 * @author ${author}
 * @date ${date}
 */
@RestController
@RequestMapping("/${table.getRawName()}")
public class ${PascalTableName}Controller {

    /**
     * 服务对象
     */
    @Resource
    private ${PascalTableName}Service ${CamelTableName}Service;

    /**
     * 通过主键查询单条数据
     *
     * @param ${pkName} 主键
     * @return 单条数据
     */
    @GetMapping("query")
    public ${PascalTableName} queryById(${pkType} ${pkName}) {
        return ${CamelTableName}Service.queryById(${pkName});
    }

    /**
     * 查询所有数据
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<${PascalTableName}> listAll() {
        return ${CamelTableName}Service.listAll();
    }

    /**
     * 新增数据
     *
     * @param ${CamelTableName} 实体对象
     * @return 新增结果
     */
    @PostMapping("add")
    public ${PascalTableName} add(@RequestBody ${PascalTableName} ${CamelTableName}) {
        return ${CamelTableName}Service.add(${CamelTableName});
    }

    /**
     * 修改数据
     *
     * @param ${CamelTableName} 实体对象
     * @return 修改结果
     */
    @PutMapping("update")
    public ${PascalTableName} edit(@RequestBody ${PascalTableName} ${CamelTableName}) {
        return ${CamelTableName}Service.edit(${CamelTableName});
    }

    /**
     * 通过主键删除数据
     *
     * @param ${pkName} 主键
     * @return 删除结果
     */
    @DeleteMapping("delete")
    public boolean deleteById(${pkType} ${pkName}) {
        return ${CamelTableName}Service.deleteById(${pkName});
    }

    /**
     * 分页查询
     *
     * @param page 分页对象
     * @return 查询结果
     */
    @PostMapping("page")
    public Page<${PascalTableName}> page(@RequestBody Page<${PascalTableName}> page) {
        return ${CamelTableName}Service.queryPage(page);
    }

}
