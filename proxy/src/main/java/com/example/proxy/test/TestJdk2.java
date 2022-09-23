package com.example.proxy.test;

import com.example.proxy.Service.UserService;
import com.example.proxy.Service.UserServiceImpl;
import com.example.proxy.config.jdk_proxy.UserServiceInterceptor;
import com.example.proxy.config.jdk_proxy.UserServiceInterceptor2;
import com.example.proxy.domain.User;

public class TestJdk2 {
    public static void main(String[] args) {
        User user = new User();
        user.setName("jdk");
        user.setAge(22);
        user.setPwd("1234567");

        UserServiceInterceptor2 userServiceInterceptor = new UserServiceInterceptor2();
        // 创建代理对象
        UserService userServiceProxy = (UserService) userServiceInterceptor.bind(new UserServiceImpl());
        System.out.println("动态代理类："+userServiceProxy.getClass());
        userServiceProxy.addUser(user);
    }
}
