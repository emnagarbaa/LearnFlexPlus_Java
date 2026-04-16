package org.example.Services;

import org.example.entities.Commentaire;
import org.example.entities.Examen;

import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements Iservice<Commentaire> {

    private Connection connection;

    public ServiceCommentaire() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ✅ AJOUTER
    @Override
    public void ajouter(Commentaire commentaire) throws SQLException {
        String sql = "INSERT INTO commentaire (contenu, datecre, auteur, nbvue, likes, examen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, commentaire.getContenu());
        ps.setDate(2, commentaire.getDatecre());
        ps.setString(3, commentaire.getAuteur());
        ps.setInt(4, commentaire.getNbvue());
        ps.setInt(5, commentaire.getLikes());
        ps.setInt(6, commentaire.getExamen_id());
        ps.executeUpdate();
        System.out.println("✅ Commentaire ajouté !");
    }
    // ✅ RÉCUPÉRER
    @Override
    public List<Commentaire> recuperer() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        try {
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
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }
    // ✅ MODIFIER
    @Override
    public void modifier(Commentaire commentaire) throws SQLException {
        String sql = "UPDATE commentaire SET contenu=?, datecre=?, auteur=?, nbvue=?, likes=?, examen_id=? " +
                "WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, commentaire.getContenu());
            ps.setDate(2, commentaire.getDatecre());
            ps.setString(3, commentaire.getAuteur());
            ps.setInt(4, commentaire.getNbvue());
            ps.setInt(5, commentaire.getLikes());
            ps.setInt(6, commentaire.getExamen_id());
            ps.setInt(7, commentaire.getId());
            ps.executeUpdate();
            System.out.println("✅ Commentaire modifié !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Commentaire commentaire) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, commentaire.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Commentaire supprimé !");
            } else {
                System.out.println("⚠️ Aucun commentaire trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    // ✅ Méthode supplémentaire — PAS de @Override car pas dans Iservice
    public void supprimerParExamenId(int examenId) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE examen_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, examenId);
            int rows = ps.executeUpdate();
            System.out.println("✅ " + rows + " commentaire(s) supprimé(s) !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public List<Commentaire> recupererParExamenId(int examenId) throws SQLException {
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