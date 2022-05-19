package com.gf.generators;

import java.util.Scanner;

/**
 * @author LiuBin
 * 扩展数据源配置可以实现Generator接口，并在枚举类中添加对应枚举。
 */
public class CodeGenerator {


    public static void main(String[] args) {
        System.out.println("请输入数据库类型");
        Scanner sc = new Scanner(System.in);
        String dbType = sc.next();
        try {
            GeneratorEnum.forEach_CountryEnum(dbType).getGenerator().init();
        } catch (Exception e) {
            e.printStackTrace();
        };
    }
}