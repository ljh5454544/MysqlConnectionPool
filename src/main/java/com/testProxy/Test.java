package com.testProxy;

import java.lang.reflect.Proxy;

public class Test {


    public static void main(String[] args) {

        Son son = new Son();

        SonProxy sonProxy = new SonProxy(son);

        SonI obj = (SonI) Proxy.newProxyInstance(son.getClass().getClassLoader(), son.getClass().getInterfaces(), sonProxy);

//        Son sons = (Son) Proxy.newProxyInstance(son.getClass().getClassLoader(), son.getClass().getInterfaces(), sonProxy);
        obj.Sing();
//
        obj.Dance();
    }
}
