package com.lagou.edu.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.lagou.edu.anno.ComponentNote;

/**
 * @author 应癫
 */
@ComponentNote
public class DruidUtils {

    private static DruidDataSource druidDataSource = new DruidDataSource();

    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/lagou");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}
