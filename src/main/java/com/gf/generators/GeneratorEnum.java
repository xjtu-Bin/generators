package com.gf.generators;

public enum GeneratorEnum {
    ORACLE("oracle",new OracleCodeGenerator()),MYSQL("mysql",new MySqlCodeGenerator());



    private String dbType;

    private Generator generator;

    GeneratorEnum(String dbType, Generator generator) {
        this.dbType = dbType;
        this.generator = generator;
    }
    public String getDbType() {
        return dbType;
    }

    public Generator getGenerator() {
        return generator;
    }

    public static GeneratorEnum forEach_CountryEnum(String dbName) {
        GeneratorEnum[] mArray = GeneratorEnum.values();
        for (GeneratorEnum element : mArray) {
            if (dbName.equals(element.getDbType())) {
                return element;
            }
        }
        System.out.println("未配置指定数据库");
        return null;
    }

}
