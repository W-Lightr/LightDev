<#-- 1. 定义模板中需要使用的通用变量 -->
<#-- 根据表名生成符合Java规范的类名 (大驼峰) -->
<#assign PascalTableName = NameUtil.toPascalCase(table.getRawName())>

<#-- 2. 配置文件名，必须放在模板顶部 -->
#region config
fileName=${PascalTableName}Mapper.xml
#endregion

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${namespace}.mapper.${PascalTableName}Mapper">


</mapper>
