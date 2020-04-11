package com.lagou.edu.container;


import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class MyContainer {

    // 单例池
    private ConcurrentHashMap<String,Object> singleMap = new ConcurrentHashMap<>();
    // 事务属性列表
    private ConcurrentHashMap<String,Field> txFields = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Object> getSingleMap() {
        return singleMap;
    }

    public ConcurrentHashMap<String, Field> getTxFields() {
        return txFields;
    }

    public MyContainer() {
    }
}
