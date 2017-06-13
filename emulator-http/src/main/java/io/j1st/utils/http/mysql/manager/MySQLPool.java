package io.j1st.utils.http.mysql.manager;

/**
 * Created by Administrator on 2016/7/18.
 */

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MySQLPool {

    private static final Logger logger = LoggerFactory.getLogger(MySQLPool.class);


    private static volatile MySQLPool pool;
    private MysqlDataSource ds;
    private Map<Connection, Boolean> map;

    private String url = null;
    private String username = null;
    private String password = null;
    private int initPoolSize = 10;
    private int maxPoolSize = 200;
    private int waitTime = 100;

    private MySQLPool() {
    }

    public static MySQLPool getInstance() {
        if (pool == null) {
            synchronized (MySQLPool.class) {
                if (pool == null) {
                    pool = new MySQLPool();
                }
            }
        }
        return pool;
    }

    public void init(AbstractConfiguration config) {
        logger.debug("开始初始化mysql数据库连接池");
        this.url = config.getString("mysql.url");
        this.username = config.getString("mysql.username");
        this.password = config.getString("mysql.password");
        this.initPoolSize = config.getInt("mysql.initSize");
        this.maxPoolSize = config.getInt("mysql.maxSize");

        try {
            ds = new MysqlDataSource();
            ds.setUrl(url);
            ds.setUser(username);
            ds.setPassword(password);
            ds.setCacheCallableStmts(true);
            ds.setConnectTimeout(1000);
            ds.setLoginTimeout(2000);
            ds.setUseUnicode(true);
            ds.setEncoding("UTF-8");
            ds.setZeroDateTimeBehavior("convertToNull");
            ds.setMaxReconnects(5);
            ds.setAutoReconnect(true);
            map = new HashMap<Connection, Boolean>();
            for (int i = 0; i < initPoolSize; i++) {
                map.put(getNewConnection(), true);
            }

            logger.debug("mysql数据库连接池初始化完成");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("mysql数据库连接池初始化失败: {}", e);
        }
    }

    public Connection getNewConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized Connection getConnection() {
        Connection conn = null;
        try {
            for (Entry<Connection, Boolean> entry : map.entrySet()) {
                if (entry.getValue()) {
                    conn = entry.getKey();
                    map.put(conn, false);
                    break;
                }
            }
            if (conn == null || conn.isClosed()) {
                if (conn.isClosed() || map.size() < maxPoolSize) {
                    map.remove(conn);
                    conn = getNewConnection();
                    map.put(conn, false);
                } else {
                    wait(waitTime);
                    conn = getConnection();
                }
            }
            logger.debug("********************* conn is closed = " + conn.isClosed());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (map.containsKey(conn)) {
                if (conn.isClosed()) {
                    map.remove(conn);
                } else {
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                    map.put(conn, true);
                }
            } else {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}