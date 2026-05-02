package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class MyDatabase {

    private static MyDatabase instance;
    private Connection connection;

    private final String url = "jdbc:mysql://localhost:3306/learnflexplus";
    private final String user = "root";
    private final String password = "";

    private MyDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database connected!");

        } catch (Exception e) {
            System.out.println("❌ DB connection failed: " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) instance = new MyDatabase();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
