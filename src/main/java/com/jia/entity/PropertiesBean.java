package com.jia.entity;

public class PropertiesBean {

    // 节点名称
    private String nodeName;
    // 驱动名称
    private String driverName;
    // url地址
    private String url;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 最大连接数
    private int maxConnections;
    // 最小连接数
    private int minConnections;
    // 初始化连接数
    private int initConnections;
    // 重连时间 单位毫秒
    private int connectionInterval;
    // 超时时间 单位毫秒
    private int timeout;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMinConnections() {
        return minConnections;
    }

    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    public int getInitConnections() {
        return initConnections;
    }

    public void setInitConnections(int initConnections) {
        this.initConnections = initConnections;
    }

    public int getConnectionInterval() {
        return connectionInterval;
    }

    public void setConnectionInterval(int connectionInterval) {
        this.connectionInterval = connectionInterval;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "PropertiesBean{" +
                "nodeName='" + nodeName + '\'' +
                ", driverName='" + driverName + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", maxConnections=" + maxConnections +
                ", minConnections=" + minConnections +
                ", initConnections=" + initConnections +
                ", connectionInterval=" + connectionInterval +
                ", timeout=" + timeout +
                '}';
    }
}
