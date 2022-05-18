package ${package.Controller};
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyName=field.name/>
        <#assign keyType=field.propertyType/>
    </#if>
</#list>
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;

<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if swagger2>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
</#if>
<#if cfg.validation>
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * <p>
 * ${table.comment!} 机构管理控制
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName?? && package.ModuleName != "">/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if swagger2>
@Api(tags = "机构管理接口", description = "${table.controllerName}")
</#if>
<#if cfg.validation>
@Validated
</#if>
@ResponseResult
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>

    private static final BopsLogger logger = new BopsLogger(${table.controllerName}.class);

    @Autowired
    private ${table.serviceImplName} ${table.serviceName};

    /**
     * 增加机构信息
     * @param ${entity?uncap_first}
     * @return
     */
     @RequestMapping(value = {"/save"}, method = RequestMethod.POST)
     @ApiOperation(value = "新增机构信息", produces = MediaType.APPLICATION_JSON_VALUE)
     public boolean save(@ApiParam(value = "机构信息") @Validated @RequestBody ${entity} ${entity?uncap_first})
     {
         logger.debug("新增机构信息");
         return ${table.serviceName}.save(${entity?uncap_first});
     }

     /**
      * 修改机构信息
      * @param ${entity?uncap_first}
      * @return
      */
     @RequestMapping(value = {"/update"}, method = RequestMethod.POST)
     @ApiOperation(value = "修改机构信息", produces = MediaType.APPLICATION_JSON_VALUE)
     public boolean update(@ApiParam(value = "机构信息") @Validated @RequestBody ${entity} ${entity?uncap_first})
     {
         logger.debug("修改机构信息");
         return ${table.serviceName}.updateById(${entity?uncap_first});
     }

     /**
      * 删除机构信息
      * @param ${keyName?lower_case}
      * @return
      */
     @RequestMapping(value = {"/delete"}, method = RequestMethod.GET)
     @ApiOperation(value = "删除机构信息", produces = MediaType.APPLICATION_JSON_VALUE)
     public boolean delete(@ApiParam(value = "机构ID", required = true) @NotBlank(message = "${keyName}不能为空") @RequestParam ${keyType} ${keyName?lower_case})
     {
         logger.debug("删除机构信息");
         return ${table.serviceName}.removeById(${keyName?lower_case});
     }

    /**
     * 根据ID查询机构信息
     * @param ${keyName?lower_case}
     * @return
     */
     @RequestMapping(value = {"/query"}, method = RequestMethod.GET)
     @ApiOperation(value = "查询机构信息", produces = MediaType.APPLICATION_JSON_VALUE)
     public ${entity} query(@ApiParam(value = "机构ID", required = true) @NotBlank(message = "${keyName}不能为空") @RequestParam ${keyType} ${keyName?lower_case})
     {
         logger.debug("查询机构信息");
         return ${table.serviceName}.getById(${keyName?lower_case});
     }
}
</#if>
