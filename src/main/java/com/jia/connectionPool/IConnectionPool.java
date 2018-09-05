package com.jia.connectionPool;


import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接池接口
 * */
public interface IConnectionPool {


    /**
     * 获取连接, 如果等待超过超时时间, 则返回null
     * */
    Connection getConnection();

    /**
     * 获取当前线程的数据库连接
     * */
    Connection getCurrentConnection();

    /**
     *      * 释放当前的数据库连接
     *      * @param conn 数据库连接对象
     *      * @throws SQLException
     * */
    void releaseConnection(Connection conn) throws SQLException;


    /**
     * 清空当前的连接池
     * */
    void destory();

    /**
     * 检查连接池可用
     * @return 连接池是否是可用的
     * */
    boolean isActive();

    /**
     * 定时器, 检查连接池
     * */
    void checkPool();

    /**
     * 获取线程池活动的连接数目
     * @reutrn 线程池活跃的连接数目
     * */
    int getActiveNum();

    /**
     * 获取空闲的连接数目
     * @return 空闲连接的数目
     * */
    int getFreeNum();
}
