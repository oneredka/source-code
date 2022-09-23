package com.example.proxy.Service;

import com.example.proxy.domain.User;

import java.util.Objects;

public class UserServiceImpl implements UserService {
    @Override
    public void addUser(User user) {
        Objects.requireNonNull(user);
        System.out.println("jdk....正在注册用户，信息为：" + user);
    }
}
