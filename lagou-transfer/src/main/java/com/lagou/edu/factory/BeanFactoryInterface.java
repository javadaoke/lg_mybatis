package com.lagou.edu.factory;

public interface BeanFactoryInterface {

    void initBean();

    Object getBean(String beanId);

    void createBean(String beanId,Class<?> clazz) throws Exception;

    void inject() throws IllegalAccessException;

    void initTx() throws NoSuchFieldException, IllegalAccessException;

}
