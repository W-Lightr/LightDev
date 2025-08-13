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
import java.time.LocalDateTime;
import lombok.Data;


/**
 * @author ${author}
 * @Description ${table.getRawComment()}
 * @date ${date}
 */
@ApiModel(description="${table.getRawComment()}")
@Data
public class ${PascalTableName} implements Serializable {
<#list columns as column>
    /**
     * ${column.getRawComment()}
     */
    @ApiModelProperty(value="${column.getRawComment()}")
    private ${column.getMapperType()} ${NameUtil.toCamelCase(column.getRawName())};

</#list>
    private static final long serialVersionUID = 1L;
}