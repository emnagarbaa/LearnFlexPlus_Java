package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String URL      = "jdbc:mysql://localhost:3306/learnflexplus";
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
        } catch (SQLException e) {
            // ✅ FIX : on affiche l'erreur complète et on met connection à null explicitement
            System.err.println("❌ Erreur de connexion DB : " + e.getMessage());
            e.printStackTrace();
            connection = null;
        }
    }

    // ✅ FIX : synchronized pour éviter les problèmes multi-thread
    public static synchronized MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    // ✅ Reconnexion automatique si connexion fermée/expirée
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("🔄 Reconnexion à la base de données...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification connexion : " + e.getMessage());
            connect(); // tentative de reconnexion
        }

        // ✅ FIX : on lève une exception claire si la connexion est toujours null
        if (connection == null) {
            throw new RuntimeException(
                    "❌ Impossible de se connecter à la base de données : " + URL +
                            "\nVérifiez que MySQL est démarré et que les credentials sont corrects."
            );
        }

        return connection;
    }
}