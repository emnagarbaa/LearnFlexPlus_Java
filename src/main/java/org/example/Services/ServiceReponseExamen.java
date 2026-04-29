package org.example.Services;

import org.example.entities.ReponseExamen;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceReponseExamen {

    private Connection connection;

    public ServiceReponseExamen() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // Ajouter une réponse d'examen
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

    // Méthode spécifique pour soumettre un examen
    public void soumettre(ReponseExamen reponse) throws SQLException {
        ajouter(reponse);
    }

    // Récupérer toutes les réponses
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
                        rs.getTimestamp("date_soumission"),
                        rs.getDouble("note"),
                        rs.getString("commentaire_correction"),
                        rs.getTimestamp("corrige_le")
                );
                reponses.add(reponse);
            }
        }
        return reponses;
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
                            rs.getTimestamp("date_soumission"),
                            rs.getDouble("note"),
                            rs.getString("commentaire_correction"),
                            rs.getTimestamp("corrige_le")
                    );
                    reponses.add(reponse);
                }
            }
        }
        return reponses;
    }

    // Corriger une réponse avec envoi d'email (version unique et corrigée)
    public void corrigerReponseAvecEmail(int reponseId, double note, String commentaire) throws SQLException {
        // 1. Récupérer les informations de la réponse et de l'étudiant
        String query = """
            SELECT 
                r.*, 
                u.id as etudiant_id,
                u.nom as etudiant_nom, 
                u.prenom as etudiant_prenom, 
                u.email as etudiant_email,
                e.titre as examen_titre,
                (SELECT COALESCE(AVG(note), 0) FROM reponse_examen WHERE examen_id = r.examen_id AND note IS NOT NULL) as moyenne_classe
            FROM reponse_examen r
            INNER JOIN users u ON r.user_id = u.id
            INNER JOIN examen e ON r.examen_id = e.id
            WHERE r.id = ?
        """;

        String emailEtudiant = null;
        String nomEtudiant = null;
        String examenTitre = null;
        double moyenneClasse = 0;
        int etudiantId = 0;

        System.out.println("🔍 Recherche de la réponse avec ID: " + reponseId);

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reponseId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    etudiantId = rs.getInt("etudiant_id");
                    emailEtudiant = rs.getString("etudiant_email");
                    String prenom = rs.getString("etudiant_prenom");
                    String nom = rs.getString("etudiant_nom");
                    nomEtudiant = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
                    examenTitre = rs.getString("examen_titre");
                    moyenneClasse = rs.getDouble("moyenne_classe");

                    System.out.println("📧 Étudiant trouvé - ID: " + etudiantId);
                    System.out.println("📧 Nom: " + nomEtudiant);
                    System.out.println("📧 Email: " + emailEtudiant);
                    System.out.println("📧 Examen: " + examenTitre);
                } else {
                    System.err.println("❌ Aucune réponse trouvée avec l'ID: " + reponseId);
                }
            }
        }

        // 2. Enregistrer la correction
        String updateQuery = "UPDATE reponse_examen SET note = ?, commentaire_correction = ?, corrige_le = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(updateQuery)) {
            pst.setDouble(1, note);
            pst.setString(2, commentaire);
            pst.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pst.setInt(4, reponseId);
            int rowsUpdated = pst.executeUpdate();
            System.out.println("✅ Correction enregistrée, lignes affectées: " + rowsUpdated);
        }

        // 3. Envoyer l'email à l'étudiant
        if (emailEtudiant != null && !emailEtudiant.isEmpty()) {
            try {
                ServiceEmail serviceEmail = new ServiceEmail();
                serviceEmail.envoyerCorrectionExamen(emailEtudiant, nomEtudiant, examenTitre, note, commentaire, moyenneClasse);
                System.out.println("✅ Email envoyé à l'étudiant: " + emailEtudiant);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ Impossible d'envoyer l'email: email étudiant null ou vide");
        }
    }

    // Récupérer une réponse avec les détails de l'étudiant et de l'examen
    public Map<String, Object> getReponseWithDetails(int reponseId) throws SQLException {
        Map<String, Object> details = new HashMap<>();
        String query = """
            SELECT r.*, u.nom as etudiant_nom, u.prenom as etudiant_prenom,
                   u.email as etudiant_email, e.titre as examen_titre, e.pdf as examen_pdf
            FROM reponse_examen r
            INNER JOIN users u ON r.user_id = u.id
            INNER JOIN examen e ON r.examen_id = e.id
            WHERE r.id = ?
        """;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reponseId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    ReponseExamen reponse = new ReponseExamen(
                            rs.getInt("id"),
                            rs.getInt("examen_id"),
                            rs.getInt("user_id"),
                            rs.getString("contenu"),
                            rs.getTimestamp("date_soumission"),
                            rs.getDouble("note"),
                            rs.getString("commentaire_correction"),
                            rs.getTimestamp("corrige_le")
                    );
                    details.put("reponse", reponse);
                    details.put("etudiant_nom", rs.getString("etudiant_nom"));
                    details.put("etudiant_prenom", rs.getString("etudiant_prenom"));
                    details.put("etudiant_email", rs.getString("etudiant_email"));
                    details.put("examen_titre", rs.getString("examen_titre"));
                    details.put("examen_pdf", rs.getString("examen_pdf"));
                }
            }
        }
        return details;
    }

    // Récupérer les statistiques pour un examen
    public Map<String, Object> getStatistiquesExamen(int examenId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String query = """
            SELECT 
                COUNT(*) as total,
                COALESCE(AVG(note), 0) as moyenne,
                COALESCE(MAX(note), 0) as max,
                COALESCE(MIN(note), 0) as min,
                COUNT(CASE WHEN note IS NOT NULL THEN 1 END) as corriges
            FROM reponse_examen 
            WHERE examen_id = ?
        """;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, examenId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    stats.put("total", rs.getInt("total"));
                    stats.put("moyenne", rs.getDouble("moyenne"));
                    stats.put("max", rs.getDouble("max"));
                    stats.put("min", rs.getDouble("min"));
                    stats.put("corriges", rs.getInt("corriges"));
                } else {
                    stats.put("total", 0);
                    stats.put("moyenne", 0.0);
                    stats.put("max", 0.0);
                    stats.put("min", 0.0);
                    stats.put("corriges", 0);
                }
            }
        }
        return stats;
    }

    // Vérifier si un étudiant a déjà soumis un examen
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

    // Récupérer la réponse d'un étudiant pour un examen spécifique
    public ReponseExamen getReponseByExamenAndUser(int examenId, int userId) throws SQLException {
        String query = "SELECT * FROM reponse_examen WHERE examen_id = ? AND user_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, examenId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new ReponseExamen(
                            rs.getInt("id"),
                            rs.getInt("examen_id"),
                            rs.getInt("user_id"),
                            rs.getString("contenu"),
                            rs.getTimestamp("date_soumission"),
                            rs.getDouble("note"),
                            rs.getString("commentaire_correction"),
                            rs.getTimestamp("corrige_le")
                    );
                }
            }
        }
        return null;
    }
}