package com.example.proxy.test;

import com.example.proxy.Service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class GetProxyGenerator {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Class<?> pGC = Class.forName("java.lang.reflect.ProxyGenerator");
        Method generateProxyC = pGC.getDeclaredMethod("generateProxyClass", String.class, Class[].class);
        generateProxyC.setAccessible(true);

        byte[] classCode = (byte[])generateProxyC.invoke(null, "TestA", new Class[]{UserService.class});

        OutputStream out = Files.newOutputStream(Path.of("TestA.class"));
        out.write(classCode);
        out.close();
    }
}
