package com.jia.connectionPool;

import com.jia.entity.PropertiesBean;
import com.jia.utils.PropertiesManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Logger log = Logger.getLogger(ConnectionManager.class);

    private static ConnectionManager dbm = null;

    /**
     * 加载驱动器名称集合
     * */
    private Set<String> drivers = new HashSet<String>();

    /**
     * 为每个节点都单独创建一个连接池
     * */
    private ConcurrentHashMap<String, IConnectionPool> pools = new ConcurrentHashMap<>();

    private ConnectionManager(){
        createPools();
    }

    /**
     *  装载JDBC驱动程序, 创建连接池
     * */
    private void createPools(){
        String nodename = PropertiesManager.getProperty("nodename");

        for (String name : nodename.split(",")){
            PropertiesBean bean = new PropertiesBean();
            bean.setNodeName(name);


            // 检测url配置
            String url = PropertiesManager.getProperty(name + ".url");
            if (url == null){
                log.error(name + "节点url为空, 请检查配置文件");
                continue;
            }
            bean.setUrl(url);

            // 检测driver配置
            String driver = PropertiesManager.getProperty(name + ".driver");
            if (driver == null){
                log.error(name + "节点driver为空, 请检查配置文件");
                continue;
            }
            bean.setDriverName(driver);

            // 验证user配置
            String user = PropertiesManager.getProperty(name + ".user");
            if (user == null){
                log.error(name + "节点用户名设置为空, 请检查配置文件");
                continue;
            }
            bean.setUsername(user);

            // 验证password配置
            String password = PropertiesManager.getProperty(name + ".password");
            if (password == null){
                log.error(name + "节点密码设置为空, 请检查配置文件");
                continue;
            }
            bean.setPassword(password);


            // 验证最小连接数目配置
            String minConnections = PropertiesManager.getProperty(name + ".minconnections");
            int minConn;

            try {
                minConn = Integer.parseInt(minConnections);
            } catch (NumberFormatException e) {
                log.error(name + "节点最小数目设置有误, 默认设置为5");
                minConn = 5;
            }
            bean.setMinConnections(minConn);

            // 验证初试连接数目配置
            String initConnections = PropertiesManager.getProperty(name + ".initconnections");
            int initConn;

            try {
                initConn = Integer.parseInt(initConnections);
            } catch (NumberFormatException e) {
                log.error(name + "节点初始连接数目设置有误, 默认设置为5");
                initConn = 5;
            }
            bean.setInitConnections(initConn);

            // 验证最大连接数目配置是否正确
            String maxConnections = PropertiesManager.getProperty(name + ".maxconnections");
            int maxConn;

            try {
                maxConn = Integer.parseInt(maxConnections);
            } catch (NumberFormatException e) {
                log.error(name + "节点最大连接数目设置有误, 默认设置为30");
                maxConn = 30;
            }
            bean.setMaxConnections(maxConn);


            // 验证重连间隔时间
            String conninterval = PropertiesManager.getProperty(name + ".conninterval");
            int  intervalTime;
            try {
                intervalTime = Integer.parseInt(conninterval);
            } catch (NumberFormatException e) {
                log.error(name + "节点重连时间设置有误, 默认设置为500ms");
                intervalTime = 500;
            }
            bean.setConnectionInterval(intervalTime);

            // 验证超时时间
            String timeout = PropertiesManager.getProperty(name + ".timeout");
            int timeoutTime;
            try {
                timeoutTime = Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                log.error(name + "节点超时时间设置有误, 默认设置为2000ms");
                timeoutTime = 2000;
            }
            bean.setConnectionInterval(timeoutTime);

            // 创建驱动
            if (!drivers.contains(bean.getDriverName())){
                try {
                    System.out.println(bean.getDriverName());
                    Class.forName(bean.getDriverName());
                    log.info("加载JDBC驱动" + bean.getDriverName()+"成功");
                    drivers.add(bean.getDriverName());
                } catch (ClassNotFoundException e) {
                    log.error("未找到JDBC驱动" + bean.getDriverName() + "请倒入相关驱动包");
                    e.printStackTrace();
                }
            }


            // 创建连接池
            IConnectionPool cp = ConnectionPool.CreateConnectionPool(bean);

            System.out.println(cp);
            if (cp != null){
                pools.put(name, cp);
                cp.checkPool();
                log.info("创建" +name+"数据库连接池成功");
            } else {
                log.info("创建" +name+"数据库连接池失败");
            }

        }
    }

    /**
     *  ConnectionManager 为单例模式
     *
     * @return 返回此类的单例
     * */
    public synchronized static ConnectionManager getInstance(){
        if (dbm == null){
            dbm = new ConnectionManager();
        }
        return dbm;
    }

    /**
     * 从指定的连接池中获取连接
     *
     * @param nodeName 连接池的节点名称
     * @return 连接池中的一个可用连接或者为null
     * */
    public Connection getConnection(String nodeName){
        IConnectionPool pool = pools.get(nodeName);
        return pool.getConnection();
    }


    /**
     * 回收指定连接池的连接
     *
     * @param poolName 连接池名称
     * @param conn 要回收的连接
     */
    public void closeConnection(String poolName, Connection conn) throws SQLException{
        IConnectionPool pool = pools.get(poolName);
        if (pool != null){
            try {
                pool.releaseConnection(conn);
            } catch (SQLException e) {
                log.error("回收"+poolName+"池中的连接失败");
                throw new SQLException(e);
            }
        }else {
            log.error("找不到"+poolName+"连接池, 无法回收");
        }
    }

    /**
     * 销毁全部的连接
     * */
    public void destory(){
        for (Map.Entry<String, IConnectionPool> poolEntry : pools.entrySet()){
            IConnectionPool pool = poolEntry.getValue();
            pool.destory();
        }
    }







}
