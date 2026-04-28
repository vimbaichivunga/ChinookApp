package com.chinook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB Driver not found", e);
        }

        String host     = System.getenv("CHINOOK_DB_HOST") != null ? System.getenv("CHINOOK_DB_HOST") : "localhost";
        String port     = System.getenv("CHINOOK_DB_PORT") != null ? System.getenv("CHINOOK_DB_PORT") : "3306";
        String name     = System.getenv("CHINOOK_DB_NAME") != null ? System.getenv("CHINOOK_DB_NAME") : "u25136608_chinook";
        String user     = System.getenv("CHINOOK_DB_USERNAME") != null ? System.getenv("CHINOOK_DB_USERNAME") : "root";
        String password = System.getenv("CHINOOK_DB_PASSWORD") != null ? System.getenv("CHINOOK_DB_PASSWORD") : "V33mba120######";

        String url = "jdbc:mariadb://" + host + ":" + port + "/" + name;
        return DriverManager.getConnection(url, user, password);
    }
}