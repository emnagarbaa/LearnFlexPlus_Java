package org.example.Services;

import org.example.entities.Challenge;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceChallenge implements Iservice<Challenge> {

    private Connection connection;

    public ServiceChallenge() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ✅ AJOUTER
    @Override
    public void ajouter(Challenge challenge) throws SQLException {
        String sql = "INSERT INTO challenge (titrec, descriptionc, objectifscore, progressionactuelle, " +
                "niveaudifficulte, niveauatteint, typerecomponse, contenurecompense, etat, " +
                "dated, datef, datelimite, alerte, question, images, dernier_score, " +
                "dernier_niveau, reponses, interacty_hash, created_at, updated_at, examen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, challenge.getTitrec());
        ps.setString(2, challenge.getDescriptionc());
        ps.setDouble(3, challenge.getObjectifscore());
        ps.setDouble(4, challenge.getProgressionactuelle());
        ps.setString(5, challenge.getNiveaudifficulte());
        ps.setString(6, challenge.getNiveauatteint());
        ps.setString(7, challenge.getTyperecomponse());
        ps.setString(8, challenge.getContenurecompense());
        ps.setString(9, challenge.getEtat());
        ps.setDate(10, challenge.getDated());
        ps.setDate(11, challenge.getDatef());
        ps.setDate(12, challenge.getDatelimite());
        ps.setBoolean(13, challenge.isAlerte());
        ps.setString(14, challenge.getQuestion());
        ps.setString(15, challenge.getImages());
        ps.setInt(16, challenge.getDernier_score());
        ps.setString(17, challenge.getDernier_niveau());
        ps.setString(18, challenge.getReponses());
        ps.setString(19, challenge.getInteracty_hash());
        ps.setInt(20, challenge.getExamen_id());
        ps.executeUpdate();
        System.out.println("✅ Challenge ajouté !");
    }

    // ✅ MODIFIER
    @Override
    public void modifier(Challenge challenge) throws SQLException {
        String sql = "UPDATE challenge SET titrec=?, descriptionc=?, objectifscore=?, progressionactuelle=?, " +
                "niveaudifficulte=?, niveauatteint=?, typerecomponse=?, contenurecompense=?, etat=?, " +
                "dated=?, datef=?, datelimite=?, alerte=?, question=?, images=?, dernier_score=?, " +
                "dernier_niveau=?, reponses=?, interacty_hash=?, updated_at=NOW() WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, challenge.getTitrec());
            ps.setString(2, challenge.getDescriptionc());
            ps.setDouble(3, challenge.getObjectifscore());
            ps.setDouble(4, challenge.getProgressionactuelle());
            ps.setString(5, challenge.getNiveaudifficulte());
            ps.setString(6, challenge.getNiveauatteint());
            ps.setString(7, challenge.getTyperecomponse());
            ps.setString(8, challenge.getContenurecompense());
            ps.setString(9, challenge.getEtat());
            ps.setDate(10, challenge.getDated());
            ps.setDate(11, challenge.getDatef());
            ps.setDate(12, challenge.getDatelimite());
            ps.setBoolean(13, challenge.isAlerte());
            ps.setString(14, challenge.getQuestion());
            ps.setString(15, challenge.getImages());
            ps.setInt(16, challenge.getDernier_score());
            ps.setString(17, challenge.getDernier_niveau());
            ps.setString(18, challenge.getReponses());
            ps.setString(19, challenge.getInteracty_hash());
            ps.setInt(20, challenge.getId());
            ps.executeUpdate();
            System.out.println("✅ Challenge modifié !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // ✅ SUPPRIMER
    @Override
    public void supprimer(Challenge challenge) throws SQLException {
        String sql = "DELETE FROM challenge WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, challenge.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Challenge supprimé !");
            } else {
                System.out.println("⚠️ Aucun challenge trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // ✅ RÉCUPÉRER
    @Override
    public List<Challenge> recuperer() throws SQLException {
        List<Challenge> list = new ArrayList<>();
        String sql = "SELECT * FROM challenge";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Challenge c = new Challenge();
                c.setId(rs.getInt("id"));
                c.setTitrec(rs.getString("titrec"));
                c.setDescriptionc(rs.getString("descriptionc"));
                c.setObjectifscore(rs.getDouble("objectifscore"));
                c.setProgressionactuelle(rs.getDouble("progressionactuelle"));
                c.setNiveaudifficulte(rs.getString("niveaudifficulte"));
                c.setNiveauatteint(rs.getString("niveauatteint"));
                c.setTyperecomponse(rs.getString("typerecomponse"));
                c.setContenurecompense(rs.getString("contenurecompense"));
                c.setEtat(rs.getString("etat"));
                c.setDated(rs.getDate("dated"));
                c.setDatef(rs.getDate("datef"));
                c.setDatelimite(rs.getDate("datelimite"));
                c.setAlerte(rs.getBoolean("alerte"));
                c.setQuestion(rs.getString("question"));
                c.setImages(rs.getString("images"));
                c.setDernier_score(rs.getInt("dernier_score"));
                c.setDernier_niveau(rs.getString("dernier_niveau"));
                c.setReponses(rs.getString("reponses"));
                c.setInteracty_hash(rs.getString("interacty_hash"));
                c.setExamen_id(rs.getInt("examen_id"));
                list.add(c);
            }
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }
    // ✅ Méthode supplémentaire — supprimer par examen_id
    public void supprimerParExamenId(int examenId) throws SQLException {
        String sql = "DELETE FROM challenge WHERE examen_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, examenId);
            int rows = ps.executeUpdate();
            System.out.println("✅ " + rows + " challenge(s) supprimé(s) !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}