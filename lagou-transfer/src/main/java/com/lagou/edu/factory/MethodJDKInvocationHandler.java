package com.lagou.edu.factory;

import com.lagou.edu.utils.TransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MethodJDKInvocationHandler implements InvocationHandler {
    private Object obj;

    private TransactionManager transactionManager;

    public MethodJDKInvocationHandler(TransactionManager transactionManager,Object object){
        this.transactionManager = transactionManager;
        this.obj = object;
    }
    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Object result = null;
        /*if(obj.getClass().getMethod(method.getName(),method.getParameterTypes()).getAnnotation(TransactionalNote.class) == null){
            return   method.invoke(obj,args);
        }*/
        try{
            // 开启事务(关闭事务的自动提交)
            transactionManager.beginTransaction();
            result = method.invoke(obj,args);
            // 提交事务
            transactionManager.commit();
        }catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            transactionManager.rollback();
            // 抛出异常便于上层servlet捕获
            throw e;
        }
        return result;
    }
}
