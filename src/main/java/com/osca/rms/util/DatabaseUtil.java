package com.osca.rms.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DatabaseUtil {

    private static final Logger logger = LogManager.getLogger(DatabaseUtil.class);

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "";
            String USER = "";
            String PASS = "";
            conn = DriverManager.getConnection(url, USER, PASS);
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
        } catch (Exception e) {
            logger.error("Database Connection Failed : " + e.toString());
        }
        return conn;
    }

    public static boolean execute(String query, Connection conn) {
        try {
            if (conn == null || conn.isClosed()) {
                logger.error("Connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Database Connection Failed : "+ e.toString());
            return false;
        }
        Statement stmt = null;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            status = true;
        } catch (SQLException e) {
            logger.error("Database Error : "+e.toString());
            status = false;
        } finally {
            close(stmt);
        }
        return status;
    }

    public static ResultSet executeQuery(String query, Connection conn) {
        ResultSet rs = null;
        try {
            if (conn == null || conn.isClosed()) {
                return null;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static void close(Connection connTest) {
        if (connTest != null) {
            try {
                connTest.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
