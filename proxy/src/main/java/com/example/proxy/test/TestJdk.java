package com.example.proxy.test;

import com.example.proxy.Service.UserService;
import com.example.proxy.Service.UserServiceImpl;
import com.example.proxy.domain.User;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import com.example.proxy.config.jdk_proxy.UserServiceInterceptor;

public class TestJdk {
    public static void main(String[] args) {
        User user = new User();
        user.setName("jdk");
        user.setAge(22);
        user.setPwd("1234567");
        // 被代理类
        UserService delegate = new UserServiceImpl();
        InvocationHandler userServiceInterceptor = new UserServiceInterceptor(delegate);
        // 动态代理类
        UserService userServiceProxy = (UserService)Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                delegate.getClass().getInterfaces(), userServiceInterceptor);
        System.out.println("动态代理类："+userServiceProxy.getClass());
        userServiceProxy.addUser(user);
    }
}
