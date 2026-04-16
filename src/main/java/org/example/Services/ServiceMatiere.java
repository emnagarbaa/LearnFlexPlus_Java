package org.example.Services;

import org.example.entities.Matiere;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatiere implements Iservice<Matiere> {

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    // Vérifie que tous les champs obligatoires de la matière sont remplis et corrects avant l'enregistrement
    private void valider(Matiere m) {
        if (m.getNomMatiere() == null || m.getNomMatiere().isBlank())
            throw new IllegalArgumentException("Le nom de la matière est obligatoire.");
        if (m.getNomMatiere().length() < 3)
            throw new IllegalArgumentException("Le nom doit contenir au moins 3 caractères.");
        if (m.getNomMatiere().length() > 255)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 255 caractères.");
        if (m.getDescription() == null || m.getDescription().isBlank())
            throw new IllegalArgumentException("La description est obligatoire.");
        if (m.getSection() == null || m.getSection().isBlank())
            throw new IllegalArgumentException("La section est obligatoire.");
        if (m.getCodeMatiere() == null || m.getCodeMatiere().isBlank())
            throw new IllegalArgumentException("Le code de la matière est obligatoire.");
        if (m.getNiveau() == null || m.getNiveau().isBlank())
            throw new IllegalArgumentException("Le niveau est obligatoire.");

        // Sécurité contre les injections XSS
        verifierContenuDangereux(m.getNomMatiere(), "nom");
        verifierContenuDangereux(m.getDescription(), "description");
    }

    // Analyse une chaîne de caractères pour détecter des balises de script potentiellement dangereuses
    private void verifierContenuDangereux(String valeur, String champ) {
        if (valeur == null) return;
        String v = valeur.toLowerCase();
        if (v.contains("<script") || v.contains("javascript:") || v.contains("onerror="))
            throw new IllegalArgumentException("Contenu dangereux détecté dans le champ : " + champ);
    }

    // Insère une nouvelle matière dans la table 'matiere' et récupère son ID généré
    @Override
    public void ajouter(Matiere matiere) throws SQLException {
        valider(matiere);

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "INSERT INTO matiere (nom_matiere, description, section, code_matiere, date_creation, niveau, image) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, matiere.getNomMatiere().trim());
        ps.setString(2, matiere.getDescription().trim());
        ps.setString(3, matiere.getSection().trim());
        ps.setString(4, matiere.getCodeMatiere().trim());
        ps.setString(5, matiere.getNiveau().trim());
        ps.setString(6, matiere.getImage());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            matiere.setId(rs.getInt(1));
        }
        System.out.println("✅ Matière ajoutée !");
    }

    // Met à jour les informations d'une matière existante à partir de son ID
    @Override
    public void modifier(Matiere matiere) throws SQLException {
        if (matiere.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");
        valider(matiere);

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "UPDATE matiere SET nom_matiere=?, description=?, section=?, code_matiere=?, niveau=?, image=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, matiere.getNomMatiere().trim());
        ps.setString(2, matiere.getDescription().trim());
        ps.setString(3, matiere.getSection().trim());
        ps.setString(4, matiere.getCodeMatiere().trim());
        ps.setString(5, matiere.getNiveau().trim());
        ps.setString(6, matiere.getImage());
        ps.setInt(7, matiere.getId());
        ps.executeUpdate();
        System.out.println("✅ Matière modifiée !");
    }

    // Supprime une matière et tous ses cours associés (pour respecter l'intégrité référentielle)
    @Override
    public void supprimer(Matiere matiere) throws SQLException {
        if (matiere == null || matiere.getId() <= 0)
            throw new IllegalArgumentException("Matière invalide ou ID manquant.");

        Connection connection = MyDatabase.getInstance().getConnection();
        // D'abord supprimer les cours liés pour éviter les erreurs de contrainte de clé étrangère
        String sqlCours = "DELETE FROM cours WHERE matiere_id=?";
        PreparedStatement psCours = connection.prepareStatement(sqlCours);
        psCours.setInt(1, matiere.getId());
        psCours.executeUpdate();

        String sql = "DELETE FROM matiere WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, matiere.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Matière supprimée !");
        else          System.out.println("⚠️ Aucune matière trouvée avec cet ID.");
    }

    // Récupère toutes les matières stockées en base de données, triées par date de création
    @Override
    public List<Matiere> recuperer() throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT * FROM matiere ORDER BY date_creation DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Matiere m = new Matiere();
            m.setId(rs.getInt("id"));
            m.setNomMatiere(rs.getString("nom_matiere"));
            m.setDescription(rs.getString("description"));
            m.setSection(rs.getString("section"));
            m.setCodeMatiere(rs.getString("code_matiere"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setNiveau(rs.getString("niveau"));
            m.setImage(rs.getString("image"));
            list.add(m);
        }
        return list;
    }

    // Récupère une matière spécifique en utilisant son identifiant unique
    public Matiere recupererParId(int id) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "SELECT * FROM matiere WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Matiere m = new Matiere();
            m.setId(rs.getInt("id"));
            m.setNomMatiere(rs.getString("nom_matiere"));
            m.setDescription(rs.getString("description"));
            m.setSection(rs.getString("section"));
            m.setCodeMatiere(rs.getString("code_matiere"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setNiveau(rs.getString("niveau"));
            m.setImage(rs.getString("image"));
            return m;
        }
        return null;
    }

    // Retourne le nombre total de cours liés à une matière donnée
    public int compterCours(int matiereId) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM cours WHERE matiere_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, matiereId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}
