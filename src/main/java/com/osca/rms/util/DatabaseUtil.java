package com.osca.rms.util;

import java.sql.*;

public class DatabaseUtil {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "");
    }

    public static int execute(String query,Connection conn)
    {
        try {
            if( conn == null || conn.isClosed() )
            {
                System.out.println("Db insertion failed!");
                return -1;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            return -1;
        }
        Statement stmt = null;
        int status = -1;
        try {
            stmt = conn.createStatement();
            stmt.execute(query);
            status = 1;
        } catch (SQLException e) {
            e.printStackTrace();
            status = -1;
        }
        finally
        {
            close(stmt);
        }
        return status;
    }

    public static ResultSet executeQuery(String query,Connection conn)
    {
        ResultSet rs = null;
        try {
            if( conn == null || conn.isClosed() )
            {
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
        finally
        {
            close(stmt);
        }
        return rs;
    }

    public static void close(Connection connTest)
    {
        if( connTest != null )
        {
            try {
                connTest.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs)
    {
        if( rs != null )
        {
            try {
                rs.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public static void close(Statement st)
    {
        if( st != null )
        {
            try {
                st.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
