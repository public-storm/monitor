package com.zwy.monitor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwy
 * @date 2023/6/29 11:24
 */
@Slf4j
public class MybatisPlusGenerator {

    private static List<String> findTableName(Environment environment, String filterName) {
        Connection conn = null;
        List<String> tableNames = null;
        ResultSet rs = null;
        try {
            String dbUrl = environment.getProperty("spring.shardingsphere.datasource.cleaner.url");
            String username = environment.getProperty("spring.shardingsphere.datasource.cleaner.username");
            String password = environment.getProperty("spring.shardingsphere.datasource.cleaner.password");
            Assert.hasLength(dbUrl, "dbUrl 不能为空");
            Assert.hasLength(username, "username 不能为空");
            Assert.hasLength(password, "password 不能为空");
            conn = init(dbUrl, username, password);
            Assert.notNull(conn, "Connection 不能为空");
            DatabaseMetaData dbMetaData = conn.getMetaData();
            rs = dbMetaData.getTables(null, null, filterName, new String[]{"TABLE"});
            tableNames = new ArrayList<>();
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            log.error("获取表名异常", e);
        } finally {
            close(conn,rs);
        }
        return tableNames;
    }

    private static Connection init(String dbUrl, String username, String password) {
        try {
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (Exception e) {
            log.error("数据库配置连接异常", e);
            return null;
        }
    }

    private static void close(Connection conn,ResultSet rs) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("数据库关闭异常", e);
        }
    }


    



}
