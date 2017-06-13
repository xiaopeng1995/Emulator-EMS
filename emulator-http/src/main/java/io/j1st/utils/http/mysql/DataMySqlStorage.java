package io.j1st.utils.http.mysql;

import io.j1st.utils.http.mysql.manager.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mysql数据操作
 * Data MySql Storage
 */
public class DataMySqlStorage {

    private static final Logger logger = LoggerFactory.getLogger(DataMySqlStorage.class);

    private ConnectionManager pool;

    public void init(ConnectionManager pool) {
        this.pool = pool;
    }


    /**
     * 写入一条用户账户信息
     *
     * @param userAccount
     * @return
     */
//    public boolean insertUserAccount(UserAccount userAccount) {
//        Connection conn = pool.getConnection();
//        PreparedStatement statement = null;
//        int count = 0;
//        String sql = "insert into user_account(user_id,balance,pay_type,pay_pass,begin_date,end_date) " +
//                "values(?,?,?,?,?,?)";
//        try {
//            statement = conn.prepareStatement(sql);
//            statement.setString(1, userAccount.getUserId());
//            statement.setDouble(2, userAccount.getBalance());
//            statement.setInt(3, userAccount.getPayType());
//            statement.setString(4, userAccount.getPayPass());
//            if (userAccount.getBeginDate() != null)
//                statement.setDate(5, new java.sql.Date(userAccount.getBeginDate().getTime()));
//            if (userAccount.getEndDate() != null)
//                statement.setDate(6, new java.sql.Date(userAccount.getEndDate().getTime()));
//
//            count = statement.executeUpdate();
//        } catch (SQLException e) {
//            logger.error("保存用户ID为 {} 的用户账户信息时出错 {}", userAccount.getUserId(), e);
//        } finally {
//            try {
//                if (statement != null)
//                    statement.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            pool.closeConnection(conn);
//        }
//        return count > 0;
//    }


    /**
     * 查询所有套餐用户Id
     *
     * @return
     */
    public List<String> getUserIds() {
        Connection conn = pool.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select name from test";
        List<String> idList = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                idList.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("查询套餐用户ID时出错 {}", e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            pool.closeConnection(conn);
        }
        return idList;
    }

}
