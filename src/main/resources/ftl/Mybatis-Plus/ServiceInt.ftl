<#-- 1. 定义模板中需要使用的通用变量 -->
<#-- 根据表名生成符合Java规范的类名 (大驼峰) 和实例名 (小驼峰) -->
<#assign PascalTableName = NameUtil.toPascalCase(table.getRawName())>
<#assign CamelTableName = NameUtil.toCamelCase(table.getRawName())>

<#-- 2. 获取主键信息，以实现方法的动态参数 -->
<#-- 判断表是否有主键 -->
<#if table.getPrimaryColumns()?has_content>
    <#-- 获取第一个主键列 -->
    <#assign pkColumn = table.getPrimaryColumns()?first>
    <#-- 获取主键的Java类型 (e.g., Long, String, Integer) -->
    <#assign pkType = pkColumn.getMapperType()>
    <#-- 获取主键的字段名，并转为小驼峰 (e.g., userId) -->
    <#assign pkName = NameUtil.toCamelCase(pkColumn.getRawName())>
<#else>
    <#-- 如果表没有主键，则使用默认的 "String id" 作为后备 -->
    <#assign pkType = "String">
    <#assign pkName = "id">
</#if>

<#-- 3. 配置文件名，必须放在模板顶部 -->
#region config
fileName=${PascalTableName}Service.java
#endregion

package ${namespace}.service.intf;

import ${namespace}.model.entity.${PascalTableName};
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * ${table.getRawComment()}(${table.getRawName()})表服务接口
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${PascalTableName}Service extends IService<${PascalTableName}> {

    /**
     * 通过主键查询单条数据
     *
     * @param ${pkName} 主键
     * @return 实例对象
     */
    ${PascalTableName} queryById(${pkType} ${pkName});

    /**
     * 查询所有数据
     *
     * @return 对象列表
     */
    List<${PascalTableName}> listAll();

    /**
     * 新增数据
     *
     * @param ${CamelTableName} 实例对象
     * @return 实例对象
     */
    ${PascalTableName} add(${PascalTableName} ${CamelTableName});

    /**
     * 修改数据
     *
     * @param ${CamelTableName} 实例对象
     * @return 实例对象
     */
    ${PascalTableName} edit(${PascalTableName} ${CamelTableName});

    /**
     * 通过主键删除数据
     *
     * @param ${pkName} 主键
     * @return 是否成功
     */
    boolean deleteById(${pkType} ${pkName});

    /**
     * 分页查询
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<${PascalTableName}> queryPage(Page<${PascalTableName}> page);
}
