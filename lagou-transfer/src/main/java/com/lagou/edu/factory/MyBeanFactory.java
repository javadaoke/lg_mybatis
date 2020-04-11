package com.lagou.edu.factory;

import com.lagou.edu.anno.*;
import com.lagou.edu.container.MyContainer;
import com.lagou.edu.utils.ClazzUtils;
import com.lagou.edu.utils.TransactionManager;

import javax.servlet.Servlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * bean工厂
 */
public class MyBeanFactory implements BeanFactoryInterface {

    private ServletContextEvent servletContextEvent;

    private MyContainer myContainer;

    private List<Method> txMethods = new ArrayList<>();

    public MyBeanFactory(ServletContextEvent servletContextEvent, MyContainer myContainer) {
        this.servletContextEvent = servletContextEvent;
        this.myContainer = myContainer;
    }

    /**
     *  实例化所有bean并存入缓存
     */
    @Override
    public void initBean(){
        // 获取目标包下的所有类
        List<String> clazzName = ClazzUtils.getClazzName("com.lagou.edu", true);
        Set<Class<?>> classes = new HashSet<>();
        for (String name : clazzName) {
            try {
                classes.add(Class.forName(name));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        // 获取所有注解类
        Set<Class<?>> annotationClass = classes.stream().filter(e -> !e.isInterface() && !e.isAnnotation()).collect(Collectors.toSet());
        annotationClass.forEach(clazz -> {
            try {
                loadBean(clazz);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        });

    }
    // 处理service注解/component注解/controller注解
    private void loadBean(Class<?> clazz) throws ServletException, InstantiationException, IllegalAccessException {
        // service注解实例化
        if(clazz.isAnnotationPresent(ServiceNote.class)){
            String serviceValue = clazz.getDeclaredAnnotation(ServiceNote.class).value();
            if(serviceValue.isEmpty()){
                Class<?>[] interfaces = clazz.getInterfaces();
                if(interfaces.length>0){
                    for (Class<?> anInterface : interfaces) {
                        serviceValue = toLowerFirstWord(anInterface.getSimpleName());
                        break;
                    }
                }else{
                    serviceValue = toLowerFirstWord(clazz.getSimpleName());
                }
            }
            createBean(serviceValue,clazz);
        }
        // component注解实例化
        if(clazz.isAnnotationPresent(ComponentNote.class)){
            String componentValue = clazz.getDeclaredAnnotation(ComponentNote.class).value();
            if(componentValue.isEmpty()){
                Class<?>[] interfaces = clazz.getInterfaces();
                if(interfaces.length>0){
                    for (Class<?> anInterface : interfaces) {
                        componentValue = toLowerFirstWord(anInterface.getSimpleName());
                        break;
                    }
                }else{
                    componentValue = toLowerFirstWord(clazz.getSimpleName());
                }
            }
            createBean(componentValue,clazz);
        }
        // controller注解实例化
        if(clazz.isAnnotationPresent(ControllerNote.class)){
            ControllerNote controller = clazz.getAnnotation(ControllerNote.class);
            String beanId = controller.value() ;
            String url = controller.url();

            Class<? extends Servlet>  servletClass = (Class<? extends Servlet>) clazz;

            // 根据当前的servletContextEvent创建servlet
            Servlet servlet = servletContextEvent.getServletContext().createServlet(servletClass);
            ServletRegistration.Dynamic dynamic = servletContextEvent.getServletContext().addServlet(beanId, servlet);
            dynamic.addMapping(url);
            // 注入servlet至容器
            this.myContainer.getSingleMap().put(beanId,servlet);
        }

    }

    @Override
    public Object getBean(String beanId) {
        return this.myContainer.getSingleMap().get(beanId);
    }

    @Override
    public void createBean(String beanId, Class<?> clazz) throws IllegalAccessException, InstantiationException {
        // 存入缓存池
        this.myContainer.getSingleMap().put(beanId,clazz.newInstance());
    }

    @Override
    public void inject() throws IllegalAccessException {
        ConcurrentHashMap<String, Object> singleMap = this.myContainer.getSingleMap();
        Collection<Object> collection = singleMap.values();
        Iterator<Object> iterator = collection.iterator();
        while (iterator.hasNext()){
            Object o = iterator.next();
            Class<?> aClass = o.getClass();
            if(aClass.isAnnotationPresent(TransactionalNote.class)){
                // 缓存事务方法
                Method[] methods = aClass.getDeclaredMethods();
                for (Method method : methods) {
                    txMethods.add(method);
                }
            }
            // 属于事务注解类
            /*Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(TransactionalNote.class)){
                    Class<?> declaringClass = method.getDeclaringClass();
                    String simpleName = getInterfaceName(declaringClass);
                    if(simpleName.isEmpty()){
                        simpleName = declaringClass.getSimpleName();
                    }
                    String txClassName = toLowerFirstWord(simpleName);
                    // 缓存事务方法
                    if(!txMap.containsKey(txClassName)){
                        txMap.put(txClassName,method);
                    }
                }
            }*/

            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                // 一般属性注入
                String fieldId = field.getName();
                Class<?> declaringClass = field.getDeclaringClass();
                String simpleName = getInterfaceName(declaringClass);
                if(simpleName.isEmpty()){
                    simpleName = declaringClass.getSimpleName();
                }
                // 得到beanId
                String beanId = toLowerFirstWord(simpleName);
                AutowiredNote autowiredNote = field.getAnnotation(AutowiredNote.class);
                if(autowiredNote != null){
                    Object bean = singleMap.get(beanId);
                    Object fieldBean = singleMap.get(fieldId);
                    field.setAccessible(true);
                    // 完成注入
                    field.set(bean,fieldBean);
                }
                // 事务属性记录
                String fieldName = field.getName();
                Object bean = singleMap.get(fieldName);
                if(bean != null && bean.getClass().isAnnotationPresent(TransactionalNote.class)){
                    // 判断是否为事务属性
                    this.myContainer.getTxFields().put(beanId,field);
                }
            }
        }
    }

    private String getInterfaceName(Class<?> declaringClass) {
        String name = "";
        Class<?>[] interfaces = declaringClass.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                name = anInterface.getSimpleName();
                break;
            }
        }
        return name;
    }

    @Override
    public void initTx() {

        txMethods.forEach((method) -> {
            //TransactionalNote transactional = method.getAnnotation(TransactionalNote.class);
            // 获取方法对应的接口
            Class<?> methodClasses = method.getDeclaringClass();
            String beanId = getInterfaceName(methodClasses);
            if(beanId.isEmpty()){
                beanId = methodClasses.getSimpleName();
            }
            // 得到beanId
            beanId = toLowerFirstWord(beanId);
            // 只会生成一个代理类
            String transactionalManageName  = methodClasses.getDeclaredAnnotation(TransactionalNote.class).value();
            ConcurrentHashMap<String, Object> singleMap = this.myContainer.getSingleMap();
            TransactionManager transactionManager = (TransactionManager) singleMap.get(transactionalManageName);
            // 获取实际类
            Object instance = singleMap.get(beanId);
            //类对应的实际 并生成代理对象
            Object proxyBean;
            Class<?>[] interfaces = instance.getClass().getInterfaces();
            if(interfaces != null && interfaces.length > 0){
                proxyBean = ProxyFactory.getJdkProxy(transactionManager,instance);
            }else{
                proxyBean = ProxyFactory.getCglibProxy(transactionManager,instance);
            }
            singleMap.put(beanId,proxyBean);
        });
        // 事务代理生成后，事务属性列表需要完成替换
        this.myContainer.getTxFields().forEach((beanId,field) -> {
            System.out.println(beanId+" ----"+field);
            AutowiredNote annotation = field.getAnnotation(AutowiredNote.class);
            if(annotation != null){
                Object o = this.myContainer.getSingleMap().get(beanId);
                Object propertyBean = this.myContainer.getSingleMap().get(field.getName());
                System.out.println(o+" ----"+propertyBean);
                field.setAccessible(true);
                try {
                    field.set(o,propertyBean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    // 首字母小写
    private String toLowerFirstWord(String name) {
        if(Character.isLowerCase(name.charAt(0))){
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
