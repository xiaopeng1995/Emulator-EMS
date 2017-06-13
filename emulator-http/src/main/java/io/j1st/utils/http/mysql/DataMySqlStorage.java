package io.j1st.utils.http.mysql;


import io.j1st.utils.http.entity.DataField;
import io.j1st.utils.http.entity.Stream;
import io.j1st.utils.http.mysql.manager.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
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
     * 写入一条主表信息 RD_REAL_TIME_DATA
     *
     * @param kzqxh
     * @return
     */
    public boolean insertRD(String id, String kzqxh, String status) {
        Connection conn = pool.getConnection();
        PreparedStatement statement = null;
        int count = 0;
        String sql = "insert into RD_REAL_TIME_DATA(DATA_ID,PRODUCT_ID,KZQXH,STATUS,UPLOAD_TIME,CREATE_TIME) " +
                "values(?,?,?,?,?,?)";
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, "xx");
            statement.setString(3, kzqxh);
            statement.setString(4, status);
            java.util.Date date = new java.util.Date();
            Timestamp tt = new Timestamp(date.getTime());
            statement.setTimestamp(5, tt);
            statement.setTimestamp(6, tt);

            count = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("保存ID为 {} 的主表数据信息时出错 {}", id, e);
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            pool.closeConnection(conn);
        }
        return count > 0;
    }


    /**
     * 写入一条从表信息 RD_DATA_FIELD
     *
     * @param datax
     * @return
     */
    public boolean insertRDdata(DataField datax) {
        Connection conn = pool.getConnection();
        PreparedStatement statement = null;
        int count = 0;
        String sql = "insert into RD_DATA_FIELD(ID,DATA_ID,CATE,FIELD_NAME,FIELD_VALUE,PREFIX,UNIT,ROW_ID,CREATE_TIME) " +
                "values(?,?,?,?,?,?,?,?,?)";
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, datax.getId());
            statement.setString(2, datax.getDataId());
            statement.setString(3, datax.getCate());
            statement.setString(4, datax.getFieldName());
            statement.setDouble(5, datax.getFieldValue());
            statement.setString(6, datax.getPrefix());
            statement.setString(7, datax.getUnit());
            statement.setInt(8, datax.getRowId());
            Timestamp tt = new Timestamp(datax.getCreateTime().getTime());
            statement.setTimestamp(9, tt);
            count = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("保存ID为 {} 的从数据信息时出错 {}", datax.getId(), e);
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            pool.closeConnection(conn);
        }
        return count > 0;
    }

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
