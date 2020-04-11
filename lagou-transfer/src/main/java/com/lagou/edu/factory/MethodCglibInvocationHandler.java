package com.lagou.edu.factory;


import com.lagou.edu.utils.TransactionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MethodCglibInvocationHandler implements MethodInterceptor {

    private Object obj;

    private TransactionManager transactionManager;

    public MethodCglibInvocationHandler(TransactionManager transactionManager, Object object){
        this.transactionManager = transactionManager;
        this.obj = object;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result = null;

        /*if(obj.getClass().getMethod(method.getName(),method.getParameterTypes()).getAnnotation(TransactionalNote.class) == null){
            return method.invoke(obj,objects);
        }*/

        try{
            // 开启事务(关闭事务的自动提交)
            transactionManager.beginTransaction();
            result = method.invoke(obj,objects);
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
