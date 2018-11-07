package com.osca.rms;

import com.osca.rms.util.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App
{
    public static void main( String[] args )
    {
        try {
            Connection connection = Database.getConnection();
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM user");
            while (rs.next()){
                System.out.println(rs.getString(2));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        System.out.println( "Hello World!" );
    }
}
