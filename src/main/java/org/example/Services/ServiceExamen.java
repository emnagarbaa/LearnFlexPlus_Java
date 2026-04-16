package org.example.Services;

import org.example.entities.Examen;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceExamen implements Iservice<Examen> {

    private Connection connection;

    public ServiceExamen() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ✅ AJOUTER
    @Override
    public void ajouter(Examen examen) throws SQLException {
        String sql = "INSERT INTO examen (titre, description, matiere, niveauexamen, datedebut, datefin, " +
                "duree, nbquestion, scoretotal, coefficient, typeexamen, etat, pdf, questions, " +
                "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, examen.getTitre());
        ps.setString(2, examen.getDescription());
        ps.setString(3, examen.getMatiere());
        ps.setString(4, examen.getNiveauexamen());
        ps.setDate(5, examen.getDatedebut() != null ? new java.sql.Date(examen.getDatedebut().getTime()) : null);
        ps.setDate(6, examen.getDatefin() != null ? new java.sql.Date(examen.getDatefin().getTime()) : null);
        ps.setInt(7, examen.getDuree());
        ps.setInt(8, examen.getNbquestion());
        ps.setDouble(9, examen.getScoretotal());
        ps.setDouble(10, examen.getCoefficient());
        ps.setString(11, examen.getTypeexamen());
        ps.setString(12, examen.getEtat());
        ps.setString(13, examen.getPdf());
        ps.setString(14, examen.getQuestions());

        ps.executeUpdate();
        System.out.println("✅ Examen ajouté !");
    }
    // ✅ RÉCUPÉRER
    @Override
    public List<Examen> recuperer() throws SQLException {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Examen e = new Examen();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setMatiere(rs.getString("matiere"));
                e.setNiveauexamen(rs.getString("niveauexamen"));
                e.setDatedebut(rs.getDate("datedebut"));
                e.setDatefin(rs.getDate("datefin"));
                e.setDuree(rs.getInt("duree"));
                e.setNbquestion(rs.getInt("nbquestion"));
                e.setScoretotal(rs.getDouble("scoretotal"));
                e.setCoefficient(rs.getDouble("coefficient"));
                e.setTypeexamen(rs.getString("typeexamen"));
                e.setEtat(rs.getString("etat"));
                e.setPdf(rs.getString("pdf"));
                e.setQuestions(rs.getString("questions"));
                examens.add(e);
            }
        } catch (SQLException e) {
            throw e;
        }
        return examens;
    }
    // ✅ MODIFIER
    @Override
    public void modifier(Examen examen) throws SQLException {
        String sql = "UPDATE examen SET titre=?, description=?, matiere=?, niveauexamen=?, " +
                "datedebut=?, datefin=?, duree=?, nbquestion=?, scoretotal=?, coefficient=?, " +
                "typeexamen=?, etat=?, pdf=?, questions=?, updated_at=NOW() WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, examen.getTitre());
            ps.setString(2, examen.getDescription());
            ps.setString(3, examen.getMatiere());
            ps.setString(4, examen.getNiveauexamen());
            ps.setDate(5, examen.getDatedebut() != null ? new java.sql.Date(examen.getDatedebut().getTime()) : null);
            ps.setDate(6, examen.getDatefin() != null ? new java.sql.Date(examen.getDatefin().getTime()) : null);
            ps.setInt(7, examen.getDuree());
            ps.setInt(8, examen.getNbquestion());
            ps.setDouble(9, examen.getScoretotal());
            ps.setDouble(10, examen.getCoefficient());
            ps.setString(11, examen.getTypeexamen());
            ps.setString(12, examen.getEtat());
            ps.setString(13, examen.getPdf());
            ps.setString(14, examen.getQuestions());
            ps.setInt(15, examen.getId());

            ps.executeUpdate();
            System.out.println("✅ Examen modifié !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    @Override
    public void supprimer(Examen examen) throws SQLException {
        // 1. Supprimer les challenges liés
        String sqlChallenge = "DELETE FROM challenge WHERE examen_id = ?";
        PreparedStatement ps1 = connection.prepareStatement(sqlChallenge);
        ps1.setInt(1, examen.getId());
        ps1.executeUpdate();

        // 2. Supprimer les commentaires liés
        String sqlCommentaire = "DELETE FROM commentaire WHERE examen_id = ?";
        PreparedStatement ps2 = connection.prepareStatement(sqlCommentaire);
        ps2.setInt(1, examen.getId());
        ps2.executeUpdate();

        // 3. Supprimer l'examen
        String sql = "DELETE FROM examen WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, examen.getId());
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✅ Examen supprimé !");
        } else {
            System.out.println("⚠️ Aucun examen trouvé avec cet ID.");
        }
    }
}