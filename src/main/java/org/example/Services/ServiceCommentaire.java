package org.example.Services;

import org.example.entities.Commentaire;
import org.example.entities.Quiz;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements Iservice<Commentaire> {

    private Connection connection;

    public ServiceCommentaire() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    private void valider(Commentaire c) {

        // 1. Champs obligatoires
        if (c.getContenu() == null || c.getContenu().isBlank())
            throw new IllegalArgumentException("Le contenu est obligatoire.");
        if (c.getAuteur() == null || c.getAuteur().isBlank())
            throw new IllegalArgumentException("L'auteur est obligatoire.");
        if (c.getExamen_id() <= 0)
            throw new IllegalArgumentException("Un examen valide est obligatoire.");

        // 2. Longueurs maximales
        if (c.getContenu().length() > 2000)
            throw new IllegalArgumentException("Le contenu ne doit pas dépasser 2000 caractères.");
        if (c.getAuteur().length() > 100)
            throw new IllegalArgumentException("L'auteur ne doit pas dépasser 100 caractères.");

        // 3. Contraintes numériques
        if (c.getNbvue() < 0)
            throw new IllegalArgumentException("Le nombre de vues ne peut pas être négatif.");
        if (c.getLikes() < 0)
            throw new IllegalArgumentException("Le nombre de likes ne peut pas être négatif.");

        // 4. Sécurité : pas de balises script
        verifierContenuDangereux(c.getContenu(), "contenu");
        verifierContenuDangereux(c.getAuteur(),  "auteur");
    }

    private void verifierContenuDangereux(String valeur, String champ) {
        if (valeur == null) return;
        String v = valeur.toLowerCase();
        if (v.contains("<script") || v.contains("javascript:") || v.contains("onerror="))
            throw new IllegalArgumentException("Contenu dangereux détecté dans le champ : " + champ);
    }

    // ══════════════════════════════════════════════
    //  AJOUTER
    // ══════════════════════════════════════════════
    @Override
    public void ajouter(Commentaire commentaire) throws SQLException {
        valider(commentaire);  // ← validation avant tout

        String sql = "INSERT INTO commentaire (contenu, datecre, auteur, nbvue, likes, examen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, commentaire.getContenu().trim());
        ps.setDate(2, commentaire.getDatecre());
        ps.setString(3, commentaire.getAuteur().trim());
        ps.setInt(4, commentaire.getNbvue());
        ps.setInt(5, commentaire.getLikes());
        ps.setInt(6, commentaire.getExamen_id());
        ps.executeUpdate();
        System.out.println("✅ Commentaire ajouté !");
    }

    // ══════════════════════════════════════════════
    //  MODIFIER
    // ══════════════════════════════════════════════
    @Override
    public void modifier(Commentaire commentaire) throws SQLException {
        if (commentaire.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");

        valider(commentaire);  // ← validation avant tout

        String sql = "UPDATE commentaire SET contenu=?, datecre=?, auteur=?, nbvue=?, likes=?, examen_id=? " +
                "WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, commentaire.getContenu().trim());
        ps.setDate(2, commentaire.getDatecre());
        ps.setString(3, commentaire.getAuteur().trim());
        ps.setInt(4, commentaire.getNbvue());
        ps.setInt(5, commentaire.getLikes());
        ps.setInt(6, commentaire.getExamen_id());
        ps.setInt(7, commentaire.getId());
        ps.executeUpdate();
        System.out.println("✅ Commentaire modifié !");
    }

    // ══════════════════════════════════════════════
    //  SUPPRIMER
    // ══════════════════════════════════════════════
    @Override
    public void supprimer(Commentaire commentaire) throws SQLException {
        if (commentaire == null || commentaire.getId() <= 0)
            throw new IllegalArgumentException("Commentaire invalide ou ID manquant.");

        String sql = "DELETE FROM commentaire WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, commentaire.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Commentaire supprimé !");
        else          System.out.println("⚠️ Aucun commentaire trouvé avec cet ID.");
    }

    // ══════════════════════════════════════════════
    //  RÉCUPÉRER
    // ══════════════════════════════════════════════
    @Override
    public List<Commentaire> recuperer() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDatecre(rs.getDate("datecre"));
            c.setAuteur(rs.getString("auteur"));
            c.setNbvue(rs.getInt("nbvue"));
            c.setLikes(rs.getInt("likes"));
            c.setExamen_id(rs.getInt("examen_id"));
            list.add(c);
        }
        return list;
    }


    // ══════════════════════════════════════════════
    //  MÉTHODES SUPPLÉMENTAIRES
    // ══════════════════════════════════════════════
    public void supprimerParExamenId(int examenId) throws SQLException {
        if (examenId <= 0)
            throw new IllegalArgumentException("ID examen invalide.");

        String sql = "DELETE FROM commentaire WHERE examen_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, examenId);
        int rows = ps.executeUpdate();
        System.out.println("✅ " + rows + " commentaire(s) supprimé(s) !");
    }

    public List<Commentaire> recupererParExamenId(int examenId) throws SQLException {
        if (examenId <= 0)
            throw new IllegalArgumentException("ID examen invalide.");

        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE examen_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, examenId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDatecre(rs.getDate("datecre"));
            c.setAuteur(rs.getString("auteur"));
            c.setNbvue(rs.getInt("nbvue"));
            c.setLikes(rs.getInt("likes"));
            c.setExamen_id(rs.getInt("examen_id"));
            list.add(c);
        }
        return list;
    }
}