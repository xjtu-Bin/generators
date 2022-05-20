# Mybatis-plus代码生成源码分析

## 总体的流程
![image](https://user-images.githubusercontent.com/69098342/168708883-d0c0e1dc-1e24-402e-965b-ada7e8f0fdaf.png)

**AutoGenerator mpg = new AutoGenerator();**

**-->mpg.execute();**

**-->templateEngine.batchOutput()**

**-->Map<String, Object> objectMap = getObjectMap(tableInfo);**

**-->writerFile(objectMap,templateFilePath,entityFile);**

关键是objectMap，里面的对象就是模板文件中可以取到的。

第一步我们需要new一个代码生成器的对象

~~~java
AutoGenerator mpg = new AutoGenerator();
~~~

进入这个AutoGenerator这个类可以看到有以下属性：

~~~java
    /**
     * 配置信息
     */
    protected ConfigBuilder config;
    /**
     * 注入配置
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected InjectionConfig injectionConfig;
    /**
     * 数据源配置
     */
    private DataSourceConfig dataSource;
    /**
     * 数据库表配置
     */
    private StrategyConfig strategy;
    /**
     * 包 相关配置
     */
    private PackageConfig packageInfo;
    /**
     * 模板 相关配置
     */
    private TemplateConfig template;
    /**
     * 全局 相关配置
     */
    private GlobalConfig globalConfig;
    /**
     * 模板引擎
     */
    private AbstractTemplateEngine templateEngine;
~~~

所以我们在代码生成类中（CodeGenerator）主要配置的就是这些属性。

## 数据库类型与JAVA类型相互转化问题

首先要关注数据源配置中**数据库类型与JAVA类型相互转换问题**。

数据源配置通过

~~~java
DataSourceConfig dsc = new DataSourceConfig();
dsc.setTypeConvert()
mpg.setDataSource(dsc);
~~~

往setTypeConvert配置不同数据源转化的对象。在源码DataSourceConfig中可以看到typeConvert是ITypeConvert类型。

~~~java
private ITypeConvert typeConvert;
~~~

ITypeConvert是一个接口，部分现实类如下：



![image-20220512153734785](C:\Users\lb\AppData\Roaming\Typora\typora-user-images\image-20220512153734785.png)

所以用户可以根据自己选择的数据源传入对象数据源类型转换的实现类。

这里使用传入OracleTypeConvert举例子，在OracleTypeConvert.java可以看到类型转换函数processTypeConvert。

~~~java
@Override
public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
    return TypeConverts.use(fieldType)
        .test(containsAny("char", "clob").then(STRING))
        .test(containsAny("date", "timestamp").then(p -> toDateType(config)))
        .test(contains("number").then(OracleTypeConvert::toNumberType))
        .test(contains("float").then(FLOAT))
        .test(contains("blob").then(BLOB))
        .test(containsAny("binary", "raw").then(BYTE_ARRAY))
        .or(STRING);
    }
~~~

但是默认的类型转化函数不用一定满足需求，所以我们可以根据自己需求对processTypeConvert进行重写：比如

~~~java
        
dsc.setTypeConvert(new OracleTypeConvert(){
        @Override
        public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
            String t = fieldType.toLowerCase();
            if (t.contains("varchar2")) {
                return DbColumnType.STRING;
            } else if (t.contains("char")) {
                return DbColumnType.BASE_CHAR;
            } else if (t.contains("clob")) {
                return DbColumnType.CLOB;
            } else if (t.contains("number")) {
                if (t.matches("number\\([1]\\)")) {
                    return DbColumnType.BASE_BOOLEAN;
                }
                else if (t.matches("number\\([2]\\)")) {
                    return DbColumnType.BASE_BYTE;
                }
                else if (t.matches("number\\([3-4]\\)")) {
                    return DbColumnType.BASE_SHORT;
                }
                else if (t.matches("number\\([5-9]\\)") || t.matches("number")) {
                    return DbColumnType.BASE_INT;
                }
                else if (t.matches("number\\(\\d+,\\d+\\)")) {
                    return DbColumnType.BASE_DOUBLE;
                }
                else if (t.matches("number\\(1[0-8]\\)")) {
                    return DbColumnType.BASE_LONG;
                }else
                {
                    return DbColumnType.BIG_DECIMAL;
                }
            } else if (t.contains("timestamp")) {
                return DbColumnType.TIMESTAMP;
            } else {
                return new OracleTypeConvert().processTypeConvert(globalConfig, fieldType);
            }
        }
    });
    mpg.setDataSource(dsc);
~~~

## 自定义属性输入

~~~java
InjectionConfig injectionConfig = new InjectionConfig() {
    //自定义属性注入:abc
    //在.ftl(或者是.vm)模板中，通过${cfg.abc}获取属性
    @Override
    public void initMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
        this.setMap(map);
    }
};
AutoGenerator mpg = new AutoGenerator();
//配置自定义属性注入
mpg.setCfg(injectionConfig);
~~~

在模板中通过以下方式取值

```xml
entity2.java.ftl
自定义属性注入abc=${cfg.abc}

entity2.java.vm
自定义属性注入abc=$!{cfg.abc}
```

**为什么可以通过cfg取值呢**？

AutoGenerator mpg = new AutoGenerator();-->mpg.execute();-->templateEngine.batchOutput()

batchOutput()部分源码可解释,可以看到objectMap.put("cfg", injectionConfig.getMap());

~~~java
        // 自定义内容
        InjectionConfig injectionConfig = getConfigBuilder().getInjectionConfig();
        if (null != injectionConfig) {
                injectionConfig.initTableMap(tableInfo);
                objectMap.put("cfg", injectionConfig.getMap());
                List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                if (CollectionUtils.isNotEmpty(focList)) {
                    for (FileOutConfig foc : focList) {
                        if (isCreate(FileType.OTHER, foc.outputFile(tableInfo))) {
                               writerFile(objectMap, foc.getTemplatePath(), foc.outputFile(tableInfo));
                            }
                        }
                    }
                }
~~~

## 自定义entity.java.ftl实现在指定字段上加上指定注解

### 源码分析

首先明确自定义需求：**在指定表字段上添加注解**。

首先思考entity.java.ftl文件中怎么获得**配置信息和表信息**？

比如为什么可以有

~~~ftl
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
~~~

为什么可以取到

~~~java
<#if table.convert>
@TableName("${table.name}")
</#if>
~~~

带着疑问debug源码，首先

~~~java
AutoGenerator mpg = new AutoGenerator();
// 各种配置，然后执行execute()方法；
mpg.execute();
~~~

进入AutoGenerator.java中看到execute()方法

~~~java
/**
* 生成代码
*/
public void execute() {
    logger.debug("==========================准备生成文件...==========================");
    // 初始化配置
    if (null == config) {
   config = new ConfigBuilder(packageInfo, dataSource, strategy, template, globalConfig);
    if (null != injectionConfig) {
          injectionConfig.setConfig(config);
            }
        }
    if (null == templateEngine) {
        // 为了兼容之前逻辑，采用 Velocity 引擎 【 默认 】
        templateEngine = new VelocityTemplateEngine();
    }
    // 模板引擎初始化执行文件输出 
    templateEngine.init(this.pretreatmentConfigBuilder(config)).mkdirs().batchOutput().open();
     logger.debug("==========================文件生成完成！！！==========================");
    }
~~~

看到出初始化配置之后会执行templateEngine的init()等方法。templateEngine类型是AbstractTemplateEngine 

进入AbstractTemplateEngine.java看到batchOutput()才是输出 java xml 文件的关键函数；batchOutput()部分函数：

~~~java
    /**
     * 输出 java xml 文件
     */
    public AbstractTemplateEngine batchOutput() {
        try {
            List<TableInfo> tableInfoList = getConfigBuilder().getTableInfoList();
            for (TableInfo tableInfo : tableInfoList) {
                Map<String, Object> objectMap = getObjectMap(tableInfo);
                Map<String, String> pathInfo = getConfigBuilder().getPathInfo();
                TemplateConfig template = getConfigBuilder().getTemplate();
                // 自定义内容
                InjectionConfig injectionConfig = getConfigBuilder().getInjectionConfig();
                if (null != injectionConfig) {
                    injectionConfig.initTableMap(tableInfo);
                    objectMap.put("cfg", injectionConfig.getMap());
                    List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                    if (CollectionUtils.isNotEmpty(focList)) {
                        for (FileOutConfig foc : focList) {
                            if (isCreate(FileType.OTHER, foc.outputFile(tableInfo))) {
                                writerFile(objectMap, foc.getTemplatePath(), foc.outputFile(tableInfo));
                            }
                        }
                    }
                }
                // Mp.java
                String entityName = tableInfo.getEntityName();
                if (null != entityName && null != pathInfo.get(ConstVal.ENTITY_PATH)) {
                    String entityFile = String.format((pathInfo.get(ConstVal.ENTITY_PATH) + File.separator + "%s" + suffixJavaOrKt()), entityName);
                    if (isCreate(FileType.ENTITY, entityFile)) {
                        writerFile(objectMap, templateFilePath(template.getEntity(getConfigBuilder().getGlobalConfig().isKotlin())), entityFile);
                    }
                }
~~~

如果想要知道objectMap的key和value可以在AbstractTemplateEngine.java中打断点查看。

### 具体实现

自定义属性注入

~~~java
// 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
                Map<String,Object> map = new HashMap<>();
                map.put("validation_NotBlank", true);
                map.put("validation_Length", true);
                this.setMap(map);
            }
        };
~~~

重写entity.java.ftl

~~~ftl
package ${package.Entity};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if cfg.validation_NotBlank>
import javax.validation.constraints.NotBlank;
</#if>
<#if cfg.validation_Length>
import org.hibernate.validator.constraints.Length;
</#if>
<#if entityLombokModel>
import lombok.Data;
import lombok.EqualsAndHashCode;
    <#if chainModel>
import lombok.experimental.Accessors;
    </#if>
</#if>

/**
 * <p>
 * ${table.comment!}
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if entityLombokModel>
@Data
    <#if superEntityClass??>
@EqualsAndHashCode(callSuper = true)
    <#else>
@EqualsAndHashCode(callSuper = false)
    </#if>
    <#if chainModel>
@Accessors(chain = true)
    </#if>
</#if>
<#if table.convert>
@TableName("${table.name}")
</#if>
<#if swagger2>
@ApiModel(value="${entity}对象", description="${table.comment!}")
</#if>
<#if superEntityClass??>
public class ${entity} extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
<#elseif activeRecord>
public class ${entity} extends Model<${entity}> {
<#else>
public class ${entity} implements Serializable {
</#if>

<#if entitySerialVersionUID>
    private static final long serialVersionUID = 1L;
</#if>
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
        <#if swagger2>
    @ApiModelProperty(value = "${field.comment}")
        <#else>
    /**
     * ${field.comment}
     */
        </#if>
    </#if>
    <#if field.keyFlag>
        <#-- 主键 -->
        <#if field.keyIdentityFlag>
    @TableId(value = "${field.annotationColumnName}", type = IdType.AUTO)
        <#elseif idType??>
    @TableId(value = "${field.annotationColumnName}", type = IdType.${idType})
        <#elseif field.convert>
    @TableId("${field.annotationColumnName}")
        </#if>
    @NotBlank(message = "${field.name}不能为空")
        <#-- 普通字段 -->
    <#elseif field.fill??>
    <#-- -----   存在字段填充设置   ----->
        <#if field.convert>
    @TableField(value = "${field.annotationColumnName}", fill = FieldFill.${field.fill})
        <#else>
    @TableField(fill = FieldFill.${field.fill})
        </#if>
    <#elseif field.convert>
    @TableField("${field.annotationColumnName}")
    </#if>
    <#-- 长度限制 -->
    <#if "TOPORG" == field.name>
    @Length(min = 32)
    </#if>
    <#-- 乐观锁注解 -->
    <#if (versionFieldName!"") == field.name>
    @Version
    </#if>
    <#-- 逻辑删除注解 -->
    <#if (logicDeleteFieldName!"") == field.name>
    @TableLogic
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

<#if !entityLombokModel>
    <#list table.fields as field>
        <#if field.propertyType == "boolean">
            <#assign getprefix="is"/>
        <#else>
            <#assign getprefix="get"/>
        </#if>
    public ${field.propertyType} ${getprefix}${field.capitalName}() {
        return ${field.propertyName};
    }

    <#if chainModel>
    public ${entity} set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
    <#else>
    public void set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
    </#if>
        this.${field.propertyName} = ${field.propertyName};
        <#if chainModel>
        return this;
        </#if>
    }
    </#list>
</#if>

<#if entityColumnConstant>
    <#list table.fields as field>
    public static final String ${field.name?upper_case} = "${field.name}";

    </#list>
</#if>
<#if activeRecord>
    @Override
    protected Serializable pkVal() {
    <#if keyPropertyName??>
        return this.${keyPropertyName};
    <#else>
        return null;
    </#if>
    }

</#if>
<#if !entityLombokModel>
    @Override
    public String toString() {
        return "${entity}{" +
    <#list table.fields as field>
        <#if field_index==0>
            "${field.propertyName}=" + ${field.propertyName} +
        <#else>
            ", ${field.propertyName}=" + ${field.propertyName} +
        </#if>
    </#list>
        "}";
    }
</#if>
}

~~~

