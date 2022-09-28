package com.example.proxy.config.cglib_proxy;

import com.example.proxy.domain.User;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class UserServiceCglibInterceptor implements MethodInterceptor {

    private Object realObject;

    public UserServiceCglibInterceptor(Object realObject) {
        super();
        this.realObject = realObject;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("进入cglib动态代理");
        if (objects != null && objects.length > 0 && objects[0] instanceof User) {
            System.out.println("在调度真实方法之前的服务!");
            User user = (User) objects[0];
            if (user.getName().length() <=1) {
                throw new RuntimeException("用户名长度必须大于1");
            }
            if (user.getPwd().length() <= 6) {
                throw new RuntimeException("用户的密码长度必须大于6");
            }
        }

        Object result = method.invoke(realObject, objects);
        System.out.println("用户注册成功。");
        return result;
    }
}
