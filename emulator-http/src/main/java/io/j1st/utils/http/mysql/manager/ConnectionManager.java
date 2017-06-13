package io.j1st.utils.http.mysql.manager;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Administrator on 2016/7/29.
 */
public final class ConnectionManager {

    // SLF4J日志
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private static ConnectionManager instance;

    public ComboPooledDataSource ds;

    private ConnectionManager() throws Exception {

    }

    public void init(AbstractConfiguration p) throws Exception {
        logger.debug("Init Mysql DataSource Pool ");
        ds = new ComboPooledDataSource();
        ds.setUser(p.getString("mysql.username"));
        ds.setPassword(p.getString("mysql.password"));
        ds.setJdbcUrl(p.getString("mysql.url"));
        ds.setDriverClass(p.getString("mysql.driverClass"));
        ds.setInitialPoolSize(Integer.parseInt(p.getString("mysql.initialPoolSize")));
        ds.setMinPoolSize(Integer.parseInt(p.getString("mysql.minPoolSize")));
        ds.setMaxPoolSize(Integer.parseInt(p.getString("mysql.maxPoolSize")));
        ds.setMaxStatements(Integer.parseInt(p.getString("mysql.maxStatements")));
        ds.setMaxIdleTime(Integer.parseInt(p.getString("mysql.maxIdleTime")));
        logger.debug("Init Mysql DataSource Success ");
    }


    public static final ConnectionManager getInstance() {
        if (instance == null) {
            try {
                instance = new ConnectionManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * 获取一个连接
     *
     * @return
     */
    public synchronized final Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 关闭连接
     *
     * @param connection
     */
    public synchronized final void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    protected void finalize() throws Throwable {
        DataSources.destroy(ds); // 关闭datasource
        super.finalize();
    }
}
