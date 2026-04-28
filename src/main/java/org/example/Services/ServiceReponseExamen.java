package org.example.Services;

import org.example.entities.ReponseExamen;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponseExamen implements Iservice<ReponseExamen> {  // ← Utilisez Iservice (i minuscule)

    private Connection connection;

    public ServiceReponseExamen() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(ReponseExamen reponse) throws SQLException {
        String query = "INSERT INTO reponse_examen (examen_id, user_id, contenu, date_soumission) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reponse.getExamenId());
            pst.setInt(2, reponse.getUserId());
            pst.setString(3, reponse.getContenu());
            pst.setTimestamp(4, reponse.getDateSoumission());

            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reponse.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void supprimer(ReponseExamen reponse) throws SQLException {
        String query = "DELETE FROM reponse_examen WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reponse.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(ReponseExamen reponse) throws SQLException {
        String query = "UPDATE reponse_examen SET contenu = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, reponse.getContenu());
            pst.setInt(2, reponse.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public List<ReponseExamen> recuperer() throws SQLException {
        List<ReponseExamen> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponse_examen ORDER BY date_soumission DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                ReponseExamen reponse = new ReponseExamen(
                        rs.getInt("id"),
                        rs.getInt("examen_id"),
                        rs.getInt("user_id"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_soumission")
                );
                reponses.add(reponse);
            }
        }
        return reponses;
    }

    // Méthode spécifique pour soumettre un examen
    public void soumettre(ReponseExamen reponse) throws SQLException {
        ajouter(reponse);
    }

    // Récupérer les réponses d'un examen spécifique
    public List<ReponseExamen> getReponsesByExamenId(int examenId) throws SQLException {
        List<ReponseExamen> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponse_examen WHERE examen_id = ? ORDER BY date_soumission DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, examenId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ReponseExamen reponse = new ReponseExamen(
                            rs.getInt("id"),
                            rs.getInt("examen_id"),
                            rs.getInt("user_id"),
                            rs.getString("contenu"),
                            rs.getTimestamp("date_soumission")
                    );
                    reponses.add(reponse);
                }
            }
        }
        return reponses;
    }

    // Vérifier si un utilisateur a déjà soumis un examen
    public boolean hasSubmitted(int examenId, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reponse_examen WHERE examen_id = ? AND user_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, examenId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}