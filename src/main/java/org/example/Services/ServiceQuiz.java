package org.example.Services;

import org.example.entities.Quiz;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceQuiz implements Iservice<Quiz> {
    private Connection connection;

    public ServiceQuiz() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Quiz quiz) {
        String sql = "INSERT INTO quiz (titre, question, duree, etat, description) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, quiz.getTitre());
            ps.setString(2, quiz.getQuestion());
            ps.setInt(3, quiz.getDuree());
            ps.setString(4, quiz.getEtat());
            ps.setString(5, quiz.getDescription());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                quiz.setId(rs.getInt(1));
            }
            System.out.println("✅ Quiz ajouté avec ID: " + quiz.getId());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Quiz quiz) throws SQLDataException {
        String sql = "UPDATE quiz SET titre = ?, question = ?, duree = ?, etat = ?, description = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, quiz.getTitre());
            ps.setString(2, quiz.getQuestion());
            ps.setInt   (3, quiz.getDuree());
            ps.setString(4, quiz.getEtat());
            ps.setString(5, quiz.getDescription());
            ps.setInt   (6, quiz.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Quiz> recuperer() {
        String sql = "SELECT * FROM quiz";
        List<Quiz> list = new ArrayList<>();

        try {
            ResultSet rs = MyDatabase.getInstance()
                    .getConnection()
                    .createStatement()
                    .executeQuery(sql);

            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setQuestion(rs.getString("question"));
                q.setDuree(rs.getInt("duree"));
                q.setEtat(rs.getString("etat"));
                System.out.println(">>> Quiz chargé ID = " + q.getId());
                list.add(q);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void supprimer(Quiz quiz) {
        try {
            PreparedStatement ps1 = connection.prepareStatement("DELETE FROM reponse WHERE quiz_id=?");
            ps1.setInt(1, quiz.getId());
            ps1.executeUpdate();

            PreparedStatement ps2 = connection.prepareStatement("DELETE FROM quiz WHERE id=?");
            ps2.setInt(1, quiz.getId());
            ps2.executeUpdate();

            System.out.println("Quiz supprimé");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
