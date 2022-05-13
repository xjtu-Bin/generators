package com.gf.output.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author lb
 * @since 2022-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("T_TEST")
@ApiModel(value="Test对象", description="")
public class Test implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机构标识")
    @TableId("ORGID")
    @NotBlank(message = "ORGID不能为空")
    private String orgid;

    @ApiModelProperty(value = "机构名称")
    @TableField("ORGNAME")
    private String orgname;

    @ApiModelProperty(value = "机构全称")
    @TableField("ORGLONGNAME")
    private String orglongname;

    @ApiModelProperty(value = "机构级别")
    @TableField("ORGLEVEL")
    private int orglevel;

    @ApiModelProperty(value = "父机构标识")
    @TableField("PARENTORGID")
    private String parentorgid;

    @ApiModelProperty(value = "投资部门标识：0 非投资部门 1 投资部门")
    @TableField("INVESTORG")
    private char investorg;

    @ApiModelProperty(value = "顶层机构标识")
    @TableField("TOPORG")
    @Length(min = 32)
    private String toporg;


}
