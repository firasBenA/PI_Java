package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/ehealth_database";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("Connected .....");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();

        return instance;

    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Reconnecting to database...");
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnected to database: ehealth_database");
            }
        } catch (SQLException e) {
            System.err.println("Failed to reconnect: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to reconnect to database", e);
        }
        return cnx;
    }
}
