package com.testProxy;

public class Son implements SonI{
    @Override
    public void Sing() {
        System.out.println("儿子喜欢唱歌");
    }

    @Override
    public void Dance() {
        System.out.println("儿子喜欢跳舞");
    }
}
