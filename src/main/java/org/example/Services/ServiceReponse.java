package org.example.Services;

import org.example.entities.Quiz;
import org.example.entities.Reponse;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponse implements Iservice<Reponse> {

    private Connection connection;

    public ServiceReponse() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reponse reponse) {
        String sql = "INSERT INTO reponse (text, est_correcte, quiz_id) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // ✔ ajout
            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrecte());
            ps.setInt(3, reponse.getQuiz().getId());
            ps.executeUpdate();

            // ── récupérer l'id généré ──────────────
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                reponse.setId(rs.getInt(1)); // ✔ on stocke l'id dans l'objet
            }

            System.out.println("✅ Réponse ajoutée avec id = " + reponse.getId());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Reponse reponse) {
        String sql = "DELETE FROM reponse WHERE id = ?";
        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            // ← vérifie d'abord si la ligne existe
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM reponse WHERE id = ?");
            check.setInt(1, reponse.getId());
            ResultSet rs = check.executeQuery();
            rs.next();
            System.out.println(">>> Lignes trouvées avec id=" + reponse.getId() + " : " + rs.getInt(1));

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, reponse.getId());
            int rows = ps.executeUpdate();

            System.out.println(">>> Rows affected : " + rows);

        } catch (SQLException e) {
            System.err.println("Erreur suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Reponse reponse) {

        System.out.println("DEBUG ID reçu = " + reponse.getId());
        try {

            // 🔍 1. Vérifier si l'ID existe
            String checkSql = "SELECT COUNT(*) FROM reponse WHERE id=?";
            PreparedStatement checkPs = connection.prepareStatement(checkSql);
            checkPs.setInt(1, reponse.getId());

            ResultSet rs = checkPs.executeQuery();
            rs.next();

            if (rs.getInt(1) == 0) {
                System.out.println("❌ ID n'existe pas en base !");
                return; // on arrête ici
            }

            // ✏️ 2. Si l'ID existe → UPDATE
            String sql = "UPDATE reponse SET text=?, est_correcte=?, quiz_id=? WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, reponse.getTexte());
            ps.setBoolean(2, reponse.isEstCorrecte());
            ps.setInt(3, reponse.getQuiz().getId());
            ps.setInt(4, reponse.getId());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                System.out.println("❌ UPDATE échoué");
            } else {
                System.out.println("✅ UPDATE OK");
            }

        } catch (SQLException e) {
            System.out.println("SQL ERROR: " + e.getMessage());
        }
    }

    @Override
    public List<Reponse> recuperer() {
        String sql = "SELECT * FROM reponse";
        List<Reponse> list = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Reponse r = new Reponse();
                r.setId(rs.getInt("id"));
                r.setTexte(rs.getString("text"));
                r.setEstCorrecte(rs.getBoolean("est_correcte"));

                Quiz q = new Quiz();
                q.setId(rs.getInt("quiz_id"));
                r.setQuiz(q);

                list.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;

    }
    public List<Reponse> recupererParQuiz(int quizId) {
        String sql = "SELECT * FROM reponse WHERE quiz_id = ?";
        List<Reponse> list = new ArrayList<>();
        try {
            Connection conn = MyDatabase.getInstance().getConnection(); // ← connexion fraîche
            System.out.println(">>> URL lecture = " + conn.getMetaData().getURL());

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idLu = rs.getInt("id");
                System.out.println(">>> Réponse lue — id=" + idLu + " texte=" + rs.getString("text"));

                Reponse r = new Reponse();
                r.setId(idLu);
                r.setTexte(rs.getString("text"));
                r.setEstCorrecte(rs.getBoolean("est_correcte"));

                Quiz q = new Quiz();
                q.setId(quizId);
                r.setQuiz(q);

                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }




}