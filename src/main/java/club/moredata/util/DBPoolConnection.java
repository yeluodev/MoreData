package club.moredata.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 要实现单例模式，保证全局只有一个数据库连接池
 * @author ylf
 *
 * 2016年10月21日
 */
public class DBPoolConnection {
    private static DBPoolConnection dbPoolConnection = null;
    private static DruidDataSource druidDataSource = null;
    
    static {
        Properties properties = loadPropertiesFile("db_server.properties");
        try {
            druidDataSource = (DruidDataSource)DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 数据库连接池单例
     * @return
     */
    public static synchronized DBPoolConnection getInstance(){
        if (null == dbPoolConnection){
            dbPoolConnection = new DBPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 返回druid数据库连接
     * @return
     * @throws SQLException
     */
    public DruidPooledConnection getConnection() throws SQLException{
        return druidDataSource.getConnection();
    }
    /**
     * @param fullFile 配置文件名
     * @return Properties对象
     */
    private static Properties loadPropertiesFile(String fullFile) {
//        String webRootPath = "H:\\Workspace\\Java\\MoreData\\src\\main\\resources";
//        String webRootPath = "D:\\JavaWorkspace\\MoreData\\src\\main\\resources";
        if (null == fullFile || "".equals(fullFile)){
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }

        String webRootPath = DBPoolConnection.class.getClassLoader().getResource("").getPath();
//        webRootPath = new File(webRootPath).getParent();
        InputStream inputStream = null;
        Properties p =null;
        try {
            inputStream = new FileInputStream(new File(webRootPath + File.separator + fullFile));
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream){
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return p;
    }
    
}