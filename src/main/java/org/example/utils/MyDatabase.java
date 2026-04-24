package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String URL = "jdbc:mysql://localhost:3306/learnflexplus";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static MyDatabase instance;

    private MyDatabase() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connecté à : " + URL);

            // Voir toutes les tables disponibles
            var rs2 = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println(">>> Tables dans la base :");
            while (rs2.next()) {
                System.out.println("    " + rs2.getString("TABLE_NAME"));
            }

            // Voir le contenu de reponse
            var st = connection.createStatement();
            var rs = st.executeQuery("SELECT id, text FROM reponse LIMIT 10");
            System.out.println(">>> Contenu table reponse :");
            boolean vide = true;
            while (rs.next()) {
                vide = false;
                System.out.println("    id=" + rs.getInt("id") + " | text=" + rs.getString("text"));
            }
            if (vide) System.out.println("    ⚠️ TABLE VIDE !");

        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    // ✅ Reconnexion automatique si connexion fermée/expirée
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("Reconnecting to database...");
                connect();
            }
        } catch (SQLException e) {
            System.out.println("Error checking connection: " + e.getMessage());
        }
        return connection;
    }
}