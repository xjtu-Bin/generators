package com.gf.generators;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.OracleTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.querys.OracleQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.*;

public class OracleCodeGenerator implements Generator{
    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("please input table name" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("error ,please input right table name" + tip + "！");
    }

    public void init() {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        //作者
        gc.setAuthor("lb");
        //打开输出目录
        gc.setOpen(false);
        //xml开启 BaseResultMap
        gc.setBaseResultMap(true);
        //xml 开启BaseColumnList
        gc.setBaseColumnList(true);
        //实体属性 Swagger2 注解
        gc.setSwagger2(true);
        //日期格式，采用Date
        gc.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(gc);
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig().setDbQuery(new OracleQuery() {
            /**
             * 重写父类预留查询自定义字段<br>
             * 这里查询的 SQL 对应父类 tableFieldsSql 的查询字段，默认不能满足你的需求请重写它<br>
             * 模板中调用：  table.fields 获取所有字段信息，
             * 然后循环字段获取 field.customMap 从 MAP 中获取注入字段如下  NULL 或者 PRIVILEGES
             */
            @Override
            public String tableFieldsSql() {
                return "SELECT A.DATA_LENGTH, A.NULLABLE, A.DATA_PRECISION, A.DATA_SCALE, A.COLUMN_NAME, CASE WHEN A.DATA_TYPE='NUMBER' THEN "
                        + "(CASE WHEN A.DATA_PRECISION IS NULL THEN A.DATA_TYPE "
                        + "WHEN NVL(A.DATA_SCALE, 0) > 0 THEN A.DATA_TYPE||'('||A.DATA_PRECISION||','||A.DATA_SCALE||')' "
                        + "ELSE A.DATA_TYPE||'('||A.DATA_PRECISION||')' END) "
                        + "ELSE A.DATA_TYPE END DATA_TYPE, B.COMMENTS,DECODE(C.POSITION, '1', 'PRI') KEY "
                        + "FROM ALL_TAB_COLUMNS A "
                        + " INNER JOIN ALL_COL_COMMENTS B ON A.TABLE_NAME = B.TABLE_NAME AND A.COLUMN_NAME = B.COLUMN_NAME AND B.OWNER = '#schema'"
                        + " LEFT JOIN ALL_CONSTRAINTS D ON D.TABLE_NAME = A.TABLE_NAME AND D.CONSTRAINT_TYPE = 'P' AND D.OWNER = '#schema'"
                        + " LEFT JOIN ALL_CONS_COLUMNS C ON C.CONSTRAINT_NAME = D.CONSTRAINT_NAME AND C.COLUMN_NAME=A.COLUMN_NAME AND C.OWNER = '#schema'"
                        + "WHERE A.OWNER = '#schema' AND A.TABLE_NAME = '%s' ORDER BY A.COLUMN_ID ";
            }
            @Override
            public String[] fieldCustom() {
                return new String[]{"DATA_LENGTH","NULLABLE","DATA_PRECISION","DATA_SCALE"};
            }
        });
        /*new DataSourceConfig().setDbQuery(new MySqlQuery() {

         *//*
            @Override
            public String[] fieldCustom() {
                return new String[]{"NULL", "PRIVILEGES"};
            }
        })*/
        dsc.setUrl("jdbc:oracle:thin:@10.128.4.128:1521:orclrr");
        // 数据库 schema name
        //dsc.setSchemaName("gifts_ut");
        // 数据库类型
        dsc.setDbType(DbType.ORACLE);
        // 驱动名称
        dsc.setDriverName("oracle.jdbc.driver.OracleDriver");
        //用户名
        dsc.setUsername("gifts_ut");
        //密码
        dsc.setPassword("gifts_ut");
        //dsc.setTypeConvert(new OracleTypeConvert());
        dsc.setTypeConvert(new OracleTypeConvert(){
            @Override
            public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                String t = fieldType.toLowerCase();
                if (t.matches("char\\([1]\\)")) {
                    return DbColumnType.BASE_BOOLEAN;
                }else if (t.contains("varchar2")) {
                    return DbColumnType.STRING;
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
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.gf.output")
                .setEntity("pojo")
                .setMapper("mapper")
                .setService("service")
                .setServiceImpl("service.impl")
                .setController("controller");
        mpg.setPackageInfo(pc);
        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
                Map<String,Object> map = new HashMap<>();
                map.put("validation", true);
                this.setMap(map);
            }
        };

        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        // String templatePath = "/templates/mapper.xml.vm";
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" +
                        tableInfo.getEntityName() + "Mapper"
                        + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);
        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig()
                .setEntity("templates/entity2.java")
                .setController("templates/controller2.java");
        //.setMapper("templates/mapper.java")
        //.setService("templates/service.java")
        //.setServiceImpl("templates/serviceImpl.java")
        //.setController("templates/controller.java");
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        //数据库表映射到实体的命名策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        //数据库表字段映射到实体的命名策略
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //lombok模型
        strategy.setEntityLombokModel(true);
        //生成 @RestController 控制器
        strategy.setRestControllerStyle(true);
        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setControllerMappingHyphenStyle(true);
        //表前缀
        strategy.setTablePrefix("t_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }
}
