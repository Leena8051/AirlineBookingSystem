package com.mycompany.airlinebookingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    //  ByteAir database
    private static final String URL = "jdbc:mysql://localhost:3306/ByteAir?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "Amnesaifndara1*";

    //  GET CONNECTION
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println(" Database Connected Successfully");
            return conn;

        } catch (SQLException e) {
            System.err.println(" Database Connection Failed: " + e.getMessage());
            return null; // return null so pages can handle null safely
        }
    }

    public static void main(String[] args) {
        getConnection();
    }
}

