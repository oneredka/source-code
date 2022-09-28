# 

动态代理是指代理关系在运行时确定的代理模式。需要注意， `JDK` 动态代理并不等价于动态代理，前者只是动态代理的实现之一，其它实现方案还有： `CGLIB`  动态代理、 `Javassist` 动态代理和 `ASM` 动态代理等。因为代理类在编译前不存在，代理关系到运行时才能确定，因此称为动态代理。

我们今天主要讨论 `JDK` 动态代理 `（Dymanic Proxy API）` ，它是  `JDK1.3`  中引入的特性，核心 `API` 是 `Proxy` 类和 `InvocationHandler` 接口。它的原理是利用反射机制在运行时生成代理类的字节码。

**首先我们创建一个 `demo`**

```java
package com.example.proxy.domain;

public class User {
    private String name;
    private Integer age;
    private String pwd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
```

创建接口和实现类

```java
package com.example.proxy.Service;

import com.example.proxy.domain.User;

public interface UserService {

    void addUser(User user);
}
```

```java
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
```

实现InvocationHandler接口

```java
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
```

方法调用

```java
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
```

输出

```java
动态代理类：class com.sun.proxy.$Proxy0
        进入动态代理
        在调度真实方法之前的服务!
        jdk....正在注册用户，信息为：User{name='jdk', age=22, pwd='1234567'}
        用户注册成功。
```

**使用JDK的动态代理需要如下几个步骤**

1. **定义需要被代理的接口**
2. **实现InvocationHandler接口, 实现具体需要被代理的逻辑**
3. **使用Proxy类创建代理类**

### 源码分析

`newProxyInstance`

```java
@CallerSensitive
public static Object newProxyInstance(ClassLoader loader,
        Class<?>[] interfaces,
        InvocationHandler h) {
        Objects.requireNonNull(h);

final Class<?> caller = System.getSecurityManager() == null
        ? null
        : Reflection.getCallerClass();

        /*
         * 获取代理类的构造函数
         */
        Constructor<?> cons = getProxyConstructor(caller, loader, interfaces);

        return newProxyInstance(caller, cons, h);
        }
```

`newProxyInstance`只是做一些基本的检查, 然后调用`getProxyConstructor`得到代理类的构造函数. 最后在`newProxyInstance`的另一个重载版本中通过构造函数创建对象.

`getProxyConstructor`

```java
private static Constructor<?> getProxyConstructor(Class<?> caller,
        ClassLoader loader,
        Class<?>... interfaces)
        {
        // optimization for single interface
        if (interfaces.length == 1) {
        Class<?> intf = interfaces[0];
        if (caller != null) {
        checkProxyAccess(caller, loader, intf);
        }
        return proxyCache.sub(intf).computeIfAbsent(
        loader,
        (ld, clv) -> new ProxyBuilder(ld, clv.key()).build()
        );
        } else {
// interfaces cloned
final Class<?>[] intfsArray = interfaces.clone();
        if (caller != null) {
        checkProxyAccess(caller, loader, intfsArray);
        }
final List<Class<?>> intfs = Arrays.asList(intfsArray);
        return proxyCache.sub(intfs).computeIfAbsent(
        loader,
        (ld, clv) -> new ProxyBuilder(ld, clv.key()).build()
        );
        }
        }
```

`build`

```java
Constructor<?> build() {
        Class<?> proxyClass = defineProxyClass(module, interfaces);
        final Constructor<?> cons;
        try {
        cons = proxyClass.getConstructor(constructorParams);
        } catch (NoSuchMethodException e) {
        throw new InternalError(e.toString(), e);
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
        public Void run() {
        cons.setAccessible(true);
        return null;
        }
        });
        return cons;
        }
```

进入到  `build` 方法之中, 可以看到通过 `defineProxyClass` 创建了代理类, 之后获取代理类的构造函数并做权限检查. 因此经过上面一系列的跳转, 我们终于进入到看到了核心方法, 也就是 `defineProxyClass`方法

`defineProxyClass`

```java
private static Class<?> defineProxyClass(Module m, List<Class<?>> interfaces) {
            String proxyPkg = null;     // package to define proxy class in
            int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

            /*
             * Record the package of a non-public proxy interface so that the
             * proxy class will be defined in the same package.  Verify that
             * all non-public proxy interfaces are in the same package.
             */
            for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    accessFlags = Modifier.FINAL;  // non-public, final
                    String pkg = intf.getPackageName();
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException(
                                "non-public interfaces from different packages");
                    }
                }
            }

            if (proxyPkg == null) {
                // all proxy interfaces are public
                proxyPkg = m.isNamed() ? PROXY_PACKAGE_PREFIX + "." + m.getName()
                                       : PROXY_PACKAGE_PREFIX;
            } else if (proxyPkg.isEmpty() && m.isNamed()) {
                throw new IllegalArgumentException(
                        "Unnamed package cannot be added to " + m);
            }

            if (m.isNamed()) {
                if (!m.getDescriptor().packages().contains(proxyPkg)) {
                    throw new InternalError(proxyPkg + " not exist in " + m.getName());
                }
            }

            /*
             * Choose a name for the proxy class to generate.
             */
            long num = nextUniqueNumber.getAndIncrement();
            String proxyName = proxyPkg.isEmpty()
                                    ? proxyClassNamePrefix + num
                                    : proxyPkg + "." + proxyClassNamePrefix + num;

            ClassLoader loader = getLoader(m);
            trace(proxyName, m, loader, interfaces);

            /*
             * Generate the specified proxy class.
             */
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                    proxyName, interfaces.toArray(EMPTY_CLASS_ARRAY), accessFlags);
            try {
                Class<?> pc = UNSAFE.defineClass(proxyName, proxyClassFile,
                                                 0, proxyClassFile.length,
                                                 loader, null);
                reverseProxyCache.sub(pc).putIfAbsent(loader, Boolean.TRUE);
                return pc;
            } catch (ClassFormatError e) {
                /*
                 * A ClassFormatError here means that (barring bugs in the
                 * proxy class generation code) there was some other
                 * invalid aspect of the arguments supplied to the proxy
                 * class creation (such as virtual machine limitations
                 * exceeded).
                 */
                throw new IllegalArgumentException(e.toString());
            }
        }
```

`defineProxyClass`代码很长, 做了很多设置,省略掉这些设置以后, 最核心的代码实际上就是两句

1. 使用`ProxyGenerator`直接在内存中生成了一个class文件
2. 调用`defineClass`方法将生成的文件加载到虚拟机中

因此实际上JDK的动态代理也是通过生成字节码并加载的方式实现的

**ProxyGenerator分析**

进入 `ProxyGenerator` 可以看到, 这个类确实就是手动写 `Class` 的各种字段在内存中拼接出来了一个 `Class` 文件. 虽然代码很复杂, 设计了很多Class文件的细节, 但是我们可以直接将生成的 `Class` 文件保存下来并直接反编译查看其中的逻辑.

由于 `ProxyGenerator` 类并不是 `public` 类, 所以不能直接访问, 但只要使用一些反射技巧, 就可以轻松的获得这个对象并调用 `generateProxyClass` 方法, 例如

```java
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
```

可以看到我们生成的 `TestA.class`

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.example.proxy.Service.UserService;
import com.example.proxy.domain.User;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class TestA extends Proxy implements UserService {
    private static Method m1;
    private static Method m2;
    private static Method m3;
    private static Method m0;

    public TestA(InvocationHandler var1) throws  {
        super(var1);
    }

    public final boolean equals(Object var1) throws  {
        try {
            return (Boolean)super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final String toString() throws  {
        try {
            return (String)super.h.invoke(this, m2, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final void addUser(User var1) throws  {
        try {
            super.h.invoke(this, m3, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final int hashCode() throws  {
        try {
            return (Integer)super.h.invoke(this, m0, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("com.example.proxy.Service.UserService").getMethod("addUser", Class.forName("com.example.proxy.domain.User"));
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
}
```

可以看到里面的 `addUser` 方法， 会调用 `invoke` 方法。所以到了这里，你悟了吗？

再来看 `cglib`

实现 `MethodInterceptor` 接口

```sql
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
```

方法调用

```sql
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
```

打印结果

```
动态代理类父类：class com.example.proxy.Service.UserServiceImpl
进入cglib动态代理
在调度真实方法之前的服务!
jdk....正在注册用户，信息为：User{name='jdk', age=22, pwd='1234567'}
用户注册成功。
```

源码学习中。。。