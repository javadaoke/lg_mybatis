package com.lagou.edu.listener;

import com.lagou.edu.container.ContainerWithServlet;
import com.lagou.edu.container.MyContainer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MyContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("容器初始化开始-------");
        //将当前servlet添加到自定义容器中
        MyContainer container = new MyContainer();
        ContainerWithServlet containerWithServlet = new ContainerWithServlet(container,servletContextEvent);
        try {
            containerWithServlet.initAllBean();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        container.getSingleMap().forEach((k,v)->{
            System.out.println(k+"--"+v);
        });
        System.out.println("容器初始化结束-------");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
