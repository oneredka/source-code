package com.example.proxy.test;

import com.example.proxy.Service.UserServiceImpl;
import com.example.proxy.config.cglib_proxy.UserServiceCglibInterceptor;
import com.example.proxy.domain.User;
import org.springframework.cglib.proxy.Enhancer;

public class TestCglib {

    public static void main(String[] args) {
        User user = new User();
        user.setName("jdk");
        user.setAge(22);
        user.setPwd("1234567");
        // 被代理的对象
        UserServiceImpl delegate = new UserServiceImpl();
        UserServiceCglibInterceptor serviceInterceptor = new UserServiceCglibInterceptor(delegate);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(delegate.getClass());
        enhancer.setCallback(serviceInterceptor);
        // 动态代理类
        UserServiceImpl cglibProxy = (UserServiceImpl)enhancer.create();
        System.out.println("动态代理类父类："+cglibProxy.getClass().getSuperclass());
        cglibProxy.addUser(user);
    }
}
