package com.lagou.edu.container;

import com.lagou.edu.factory.MyBeanFactory;

import javax.servlet.ServletContextEvent;

public class ContainerWithServlet {

    private MyContainer myContainer;

    private ServletContextEvent servletContextEvent;

    public ContainerWithServlet(MyContainer myContainer, ServletContextEvent servletContextEvent) {
        this.myContainer = myContainer;
        this.servletContextEvent = servletContextEvent;
    }

    public void initAllBean() throws IllegalAccessException {
        MyBeanFactory beanFactory = new MyBeanFactory(servletContextEvent,myContainer);

        beanFactory.initBean();

        beanFactory.inject();

        beanFactory.initTx();

    }


}
