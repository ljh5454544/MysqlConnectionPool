package com.testProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
public class SonProxy implements InvocationHandler {

    SonI son;


    public SonProxy(SonI son){
        this.son = son;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("Sing".equals(method.getName())){

            System.out.println("开始唱歌之前");

            method.invoke(son, args);

            System.out.println("唱歌之后");
        }

        if ("Dance".equals(method.getName())){

            System.out.println("开始跳舞之前");

            method.invoke(son, args);

            System.out.println("跳舞之后");
        }

        return null;
    }
}
