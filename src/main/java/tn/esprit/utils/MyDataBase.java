package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3310/ehealth";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected ...");
        } catch (SQLException e) {
            System.out.println("Erreur fel basa ");
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}