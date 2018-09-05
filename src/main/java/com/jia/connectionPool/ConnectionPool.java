package com.jia.connectionPool;

import com.jia.entity.PropertiesBean;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 友元类, 不提供给程序直接访问
 * */
public class  ConnectionPool implements IConnectionPool {

    // 打印日志
    private static final Logger log = Logger.getLogger(ConnectionPool.class);

    // 解析properties文件所得到的节点实体类
    private PropertiesBean propertiesBean = null;

    // 连接池可用状态
    private Boolean isActive = true;

    // 空闲连接池 这里删除插入比较频繁 使用LinkedList比较好
    private LinkedList<Connection> freeConnections = new LinkedList<Connection>();

    // 活动的连接池 活动连接数 <= 允许的最大的连接数 maxConnections;
    private LinkedList<Connection> activeConnections = new LinkedList<Connection>();

    // 当前线程获得的连接
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();


    private ConnectionPool() {
        super();
    }

    public static ConnectionPool CreateConnectionPool(PropertiesBean propertiesBean){
        ConnectionPool connectionPool = new ConnectionPool();
        connectionPool.propertiesBean = propertiesBean;

        // 初始化时根据配置中的配置的初始连接数目创建指定数目的连接
        for (int i  = 0; i < connectionPool.propertiesBean.getInitConnections(); i++){
            try {
                Connection conn = connectionPool.newConnection();
                connectionPool.freeConnections.add(conn);
            } catch (SQLException e) {
                log.error(connectionPool.propertiesBean.getNodeName()+"节点连接池初始化失败");
                return null;
            }
        }
        connectionPool.isActive = true;
        return connectionPool;
    }

    /**
     * 创建一个新的连接
     * @return 数据库连接对象
     * @throws ClassNotFoundException
     * @throws SQLException
     * */
    private Connection newConnection() throws SQLException{
        Connection conn = null;

        if (this.propertiesBean != null){
            try {
                conn = DriverManager.getConnection(this.propertiesBean.getUrl(),
                        this.propertiesBean.getUsername(),
                        this.propertiesBean.getPassword());
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }

        if (conn != null){

            ConnProxy connProxy = new ConnProxy(conn, this);
            conn = connProxy.newInstance();
        }
        return conn;
    }

    /**
     * 检验连接是否有效
     * @param  connection 连接对象
     * @return 连接是否有效的判断
     * */
    private Boolean isValidConnection(Connection connection) throws SQLException{
        try {
            if (connection == null || connection.isClosed()){
                return false;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return true;
    }


    public synchronized Connection getConnection() {
        Connection conn = null;

        if (this.getActiveNum() < this.propertiesBean.getMaxConnections()){
            // 当前活跃的连接没有达到最大的连接数
            // 在连接池没有达到最大的连接数之前, 如果有可用的空闲连接就直接使用空闲连接, 否则创建新的连接
            if (this.getFreeNum() > 0){
                log.info("从空闲连接池中获取连接");
                conn = this.freeConnections.pollFirst();

                //连接闲置久了也会超时，因此空闲池中的有效连接会越来越少，需要另一个进程进行扫描监测，不断保持一定数量的可用连接
                //在下面定义了checkFreepools的TimerTask类，在checkPool()方法中进行调用
                // 由于数据库连接闲置久了会超时关闭，因此需要连接池采用机制保证每次请求的连接都是有效可用的
                try {
                    if (this.isValidConnection(conn)){
                        this.activeConnections.add(conn);
                        currentConnection.set(conn);
                    }else{
                        // 同步方法, 可重入锁
                        conn = getConnection();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else{
                log.info("空闲连接池无可用连接, 创建新的连接");
                try {
                    conn = this.newConnection();
                    this.activeConnections.add(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else{
            // 已经达到最大连接数
            // 当前连接池中的活动的连接数目已经达到最大的连接数目, 新的请求进入等待状态, 直到有空闲连接
            log.info("当前已经达到最大的连接数");
            long startTime = System.currentTimeMillis();

            try {
                // 进入等待状态 等待notify() 或者 notifyAll()的调用 或者等待了超过重连时间
                this.wait(this.propertiesBean.getConnectionInterval());
            } catch (InterruptedException e) {
                log.error("线程等待被打断");
            }
            if (this.propertiesBean.getTimeout() != 0){
                if (System.currentTimeMillis() - startTime > this.propertiesBean.getTimeout()){
                    return null;
                }
            }
            conn = this.getConnection();
        }
        return conn;
    }

    public Connection getCurrentConnection() {
        Connection connection = currentConnection.get();
        try {
            if ( !isValidConnection(connection)){
                connection = this.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized void releaseConnection(Connection conn) throws SQLException {
        log.info(Thread.currentThread().getName() +"关闭连接"+ conn);


        Connection connection = this.activeConnections.get(0);

        System.out.println(connection == conn);
        System.out.println(connection.equals(conn));

        boolean b = this.activeConnections.remove(conn);

        this.currentConnection.remove();

        int size = this.activeConnections.size();

        if ( !isValidConnection(conn)){
            freeConnections.add(conn);
        }else{
            freeConnections.add(this.newConnection());
        }
        this.notifyAll();
    }

    public synchronized void destory() {
        for (Connection conn : this.freeConnections){
            try {
                if (this.isValidConnection(conn)){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Connection conn : this.activeConnections){
            try {
                if (this.isValidConnection(conn)){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.isActive = false;
        this.freeConnections.clear();
        this.activeConnections.clear();
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void checkPool() {
        final String nodeName = this.propertiesBean.getNodeName();
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);

        // 开启一个定时器线程输出状态
        ses.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(nodeName +"空闲连接数:"+ getFreeNum());
                System.out.println(nodeName +"活动连接数:"+ getActiveNum());
            }
        }, 1, 1, TimeUnit.SECONDS);



        // 开启一个定时器线程, 监控并维持空闲池中的最小连接数
        ses.scheduleAtFixedRate(new checkFreePools(this), 1 , 5, TimeUnit.SECONDS);
    }

    class checkFreePools extends TimerTask {
        private ConnectionPool conpool = null;

        public  checkFreePools (ConnectionPool conpool){
            this.conpool = conpool;
        }
        @Override
        public void run() {
            if (this.conpool != null && this.conpool.isActive){
                int poolstotalnum = conpool.getFreeNum() + conpool.getActiveNum();
                int subnum = conpool.propertiesBean.getMinConnections() - poolstotalnum;

                if (subnum > 0){
                    System.out.println(conpool.propertiesBean.getNodeName() + "维持最小连接数目, 需要补充"+ subnum + "个连接");

                    for (int a = 0; a < subnum; a++){
                        try {
                            conpool.freeConnections.add(conpool.newConnection());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public int getActiveNum() {
        return this.activeConnections.size();
    }

    public int getFreeNum() {
        return this.freeConnections.size();
    }


    class ConnProxy implements InvocationHandler{

        Connection conn;
        ConnectionPool cp;
        Connection con;

        ConnProxy(Connection conn,ConnectionPool cp){
            this.conn = conn;
            this.cp = cp;
        }

        public Connection newInstance(){
            con =  (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(), conn.getClass().getInterfaces(), this);
            return con;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if ("close".equals(method.getName())){
                cp.releaseConnection(con);
                return null;
            }
            if ("equals".equals(method.getName())){
                Connection  connection = (Connection) args[0];
                method.invoke(con, connection);

                return null;
            }
            return method.invoke(conn, args);
        }
    }
}
