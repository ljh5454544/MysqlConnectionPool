import com.jia.connectionPool.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test0010 {


    public static void main(String[] args) throws InterruptedException {


        List<Thread> threadList = new ArrayList<Thread>();
//
//        for (int i = 1; i < 4; i++){
//            Thread t = new Thread(new workrun(i));
//            t.start();
//            threadList.add(t);
//        }
//        for (Iterator<Thread> iterator = threadList.iterator(); ((Iterator) iterator).hasNext();){
//            Thread thread = iterator.next();
//            thread.join();
//        }

        Thread t = new Thread(new workrun(1));
        t.start();
    }
}

class workrun implements Runnable{
    int i;
    public workrun(int i){
        this.i=i;
    }
    @Override
    public void run() {
        ConnectionManager cm = ConnectionManager.getInstance();

        //1.从数据池中获取数据库连接
        Connection conn = cm.getConnection("default");

        System.out.println("线程 " + Thread.currentThread().getName() + "获得连接：" + conn);

        //模拟查询耗时操作
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //2.获取用于向数据库发送sql语句的statement
        Statement st = null;
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "select * from student where id="+901;
        //3.向数据库发sql,并获取代表结果集的resultset
        //4.取出结果集的数据
        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            while(rs.next()){
                System.out.println("name=" + rs.getObject("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //模拟查询耗时操作
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + "关闭资源");
        //5.关闭链接，释放资源
        try {
            rs.close();
            st.close();
            conn.close();
//            cm.closeConnection("default",conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}