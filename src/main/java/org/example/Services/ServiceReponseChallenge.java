package org.example.Services;

import org.example.entities.ReponseChallenge;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponseChallenge {

    private final Connection cnx = MyDatabase.getInstance().getConnection();

    // ── Soumettre une réponse (étudiant) ──────────────────
    public void soumettre(ReponseChallenge r) throws SQLException {
        String sql = "INSERT INTO reponse_challenge (challenge_id, user_id, reponse_texte, statut) " +
                "VALUES (?, ?, ?, 'En attente')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getChallengeId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getReponseTexte());
            ps.executeUpdate();
        }
    }

    // ── Corriger une réponse (prof) ───────────────────────
    public void corriger(int id, String statut, Float note, String commentaire) throws SQLException {
        String sql = "UPDATE reponse_challenge " +
                "SET statut=?, note=?, commentaire=?, date_correction=NOW() " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            if (note != null) ps.setFloat(2, note); else ps.setNull(2, Types.FLOAT);
            ps.setString(3, commentaire);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    // ── Toutes les réponses d'un challenge (vue prof) ─────
    public List<ReponseChallenge> getParChallenge(int challengeId) throws SQLException {
        List<ReponseChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse_challenge WHERE challenge_id=? ORDER BY date_soumission DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Réponses en attente uniquement ────────────────────
    public List<ReponseChallenge> getEnAttente() throws SQLException {
        List<ReponseChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse_challenge WHERE statut='En attente' ORDER BY date_soumission ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Réponses d'un étudiant ────────────────────────────
    public List<ReponseChallenge> getParUser(int userId) throws SQLException {
        List<ReponseChallenge> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse_challenge WHERE user_id=? ORDER BY date_soumission DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Mapper ResultSet → entité ─────────────────────────
    private ReponseChallenge map(ResultSet rs) throws SQLException {
        ReponseChallenge r = new ReponseChallenge();
        r.setId(rs.getInt("id"));
        r.setChallengeId(rs.getInt("challenge_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setReponseTexte(rs.getString("reponse_texte"));
        r.setDateSoumission(rs.getTimestamp("date_soumission"));
        r.setStatut(rs.getString("statut"));
        float note = rs.getFloat("note");
        r.setNote(rs.wasNull() ? null : note);
        r.setCommentaire(rs.getString("commentaire"));
        r.setDateCorrection(rs.getTimestamp("date_correction"));
        return r;
    }
}
