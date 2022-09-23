package com.example.proxy.config.jdk_proxy;

import com.example.proxy.domain.User;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UserServiceInterceptor2 implements InvocationHandler {

    private Object realObject;

    /**
     * 生成代理对象的方法
     */
    public Object bind(Object realObject) {
        this.realObject = realObject;
        return Proxy.newProxyInstance(realObject.getClass().getClassLoader(),
                realObject.getClass().getInterfaces(),
                this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("进入动态代理");
        if (args != null && args.length > 0 && args[0] instanceof User) {
            System.out.println("在调度真实方法之前的服务!");
            User user = (User) args[0];
            if (user.getName().length() <= 1) {
                throw new RuntimeException("用户名长度必须大于1");
            }
            if (user.getPwd().length() <= 6) {
                throw new RuntimeException("用户的密码长度必须大于6");
            }
        }
        Object result = method.invoke(realObject, args);
        System.out.println("用户注册成功。");
        return result;
    }
}
