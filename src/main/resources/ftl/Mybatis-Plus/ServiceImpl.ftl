<#-- 1. 定义模板中需要使用的通用变量 -->
<#-- 根据表名生成符合Java规范的类名 (大驼峰) 和实例名 (小驼峰) -->
<#assign PascalTableName = NameUtil.toPascalCase(table.getRawName())>
<#assign CamelTableName = NameUtil.toCamelCase(table.getRawName())>

<#-- 2. 获取主键信息，这是模板通用性的关键 -->
<#-- 判断表是否有主键 -->
<#if table.getPrimaryColumns()?has_content>
    <#-- 获取第一个主键列 -->
    <#assign pkColumn = table.getPrimaryColumns()?first>
    <#-- 获取主键的Java类型 (e.g., Long, String, Integer) -->
    <#assign pkType = pkColumn.getMapperType()>
    <#-- 获取主键的字段名，并转为小驼峰 (e.g., userId) -->
    <#assign pkName = NameUtil.toCamelCase(pkColumn.getRawName())>
    <#-- 动态生成主键的getter方法名 (e.g., getId, getUserId) -->
    <#assign pkGetterMethod = "get" + NameUtil.toPascalCase(pkName)>
<#else>
    <#-- 如果表没有主键，则使用默认的 "String id" 作为后备 -->
    <#assign pkType = "String">
    <#assign pkName = "id">
    <#assign pkGetterMethod = "getId">
</#if>

<#-- 3. 配置文件名，必须放在模板顶部 -->
#region config
fileName=${PascalTableName}ServiceImpl.java
#endregion

package ${namespace}.service.impl;

import ${namespace}.mapper.${PascalTableName}Mapper;
import ${namespace}.model.entity.${PascalTableName};
import ${namespace}.service.intf.${PascalTableName}Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * ${table.getRawComment()}(${table.getRawName()})表服务实现类
 *
 * @author ${author}
 * @date ${date}
 */
@Service
public class ${PascalTableName}ServiceImpl extends ServiceImpl<${PascalTableName}Mapper, ${PascalTableName}> implements ${PascalTableName}Service {

    @Autowired
    private ${PascalTableName}Mapper ${CamelTableName}Mapper;

    /**
     * 通过主键查询单条数据
     *
     * @param ${pkName} 主键
     * @return 实例对象
     */
    @Override
    public ${PascalTableName} queryById(${pkType} ${pkName}) {
        return this.getById(${pkName});
    }

    /**
     * 查询所有数据
     *
     * @return 对象列表
     */
    @Override
    public List<${PascalTableName}> listAll() {
        return this.list();
    }

    /**
     * 新增数据
     *
     * @param ${CamelTableName} 实例对象
     * @return 实例对象
     */
    @Override
    public ${PascalTableName} add(${PascalTableName} ${CamelTableName}) {
        this.save(${CamelTableName});
        return ${CamelTableName};
    }

    /**
     * 修改数据
     *
     * @param ${CamelTableName} 实例对象
     * @return 实例对象
     */
    @Override
    public ${PascalTableName} edit(${PascalTableName} ${CamelTableName}) {
        this.updateById(${CamelTableName});
        // 动态调用主键的getter方法，以支持各种主键名（如id, user_id等）
        return this.queryById(${CamelTableName}.${pkGetterMethod}());
    }

    /**
     * 通过主键删除数据
     *
     * @param ${pkName} 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(${pkType} ${pkName}) {
        return this.removeById(${pkName});
    }

    /**
     * 分页查询 (支持按实体作为查询条件)
     *
     * @param page 分页对象，其中 page.records[0] 可作为查询条件
     * @return 查询结果
     */
    @Override
    public Page<${PascalTableName}> queryPage(Page<${PascalTableName}> page) {
        QueryWrapper<${PascalTableName}> queryWrapper = new QueryWrapper<>();
        // 如果分页对象中携带了查询条件实体，则使用该实体进行查询
        List<${PascalTableName}> records = page.getRecords();
        if (records != null && !records.isEmpty() && records.get(0) != null) {
            queryWrapper.setEntity(records.get(0));
        }
        return this.page(page, queryWrapper);
    }
}
