<#-- 1. 定义模板中需要使用的通用变量 -->
<#-- 根据表名生成符合Java规范的类名 (大驼峰) -->
<#assign PascalTableName = NameUtil.toPascalCase(table.getRawName())>

<#-- 2. 配置文件名，必须放在模板顶部 -->
#region config
fileName=${PascalTableName}Mapper.java
#endregion

package ${namespace}.mapper;

import ${namespace}.model.entity.${PascalTableName};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ${table.getRawComment()}(${table.getRawName()})表数据库访问层
 * @author ${author}
 * @date ${date}
 */
@Mapper
public interface ${PascalTableName}Mapper extends BaseMapper<${PascalTableName}> {

}
