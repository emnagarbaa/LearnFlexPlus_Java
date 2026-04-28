package org.example.Services;

import org.example.entities.LeaderboardEntry;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLeaderboard {

    private final Connection conn = MyDatabase.getInstance().getConnection();

    // ── Ajouter un score ──────────────────────────────────────
    public void ajouterScore(LeaderboardEntry entry) throws SQLException {
        String sql = "INSERT INTO challenge_result (score, badges, created_at, reponses, user_id, challenge_id) " +
                "VALUES (?, ?, NOW(), ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, entry.getScore());
            ps.setString(2, entry.getBadges());
            ps.setString(3, entry.getReponses());
            // user_id nullable
            if (entry.getUserId() == 0) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, entry.getUserId());
            }
            ps.setInt(5, entry.getChallengeId());
            ps.executeUpdate();
        }
    }
    // ── Top 10 par challenge (trié par score DESC) ────────────
    public List<LeaderboardEntry> getTop10(int challengeId) throws SQLException {
        String sql = "SELECT * FROM challenge_result WHERE challenge_id = ? " +
                "ORDER BY score DESC LIMIT 10";
        List<LeaderboardEntry> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LeaderboardEntry e = new LeaderboardEntry();
                e.setId         (rs.getInt      ("id"));
                e.setScore      (rs.getInt      ("score"));
                e.setBadges     (rs.getString   ("badges"));
                e.setCreatedAt  (rs.getTimestamp("created_at"));
                e.setReponses   (rs.getString   ("reponses"));
                e.setUserId     (rs.getInt      ("user_id"));
                e.setChallengeId(rs.getInt      ("challenge_id"));
                list.add(e);
            }
        }
        return list;
    }

    // ── Tous les scores par challenge ─────────────────────────
    public List<LeaderboardEntry> getByChallengeId(int challengeId) throws SQLException {
        String sql = "SELECT * FROM challenge_result WHERE challenge_id = ? " +
                "ORDER BY score DESC";
        List<LeaderboardEntry> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LeaderboardEntry e = new LeaderboardEntry();
                e.setId         (rs.getInt      ("id"));
                e.setScore      (rs.getInt      ("score"));
                e.setBadges     (rs.getString   ("badges"));
                e.setCreatedAt  (rs.getTimestamp("created_at"));
                e.setReponses   (rs.getString   ("reponses"));
                e.setUserId     (rs.getInt      ("user_id"));
                e.setChallengeId(rs.getInt      ("challenge_id"));
                list.add(e);
            }
        }
        return list;
    }

    // ── Meilleur score pour un challenge ──────────────────────
    public LeaderboardEntry getMeilleurScore(int challengeId) throws SQLException {
        String sql = "SELECT * FROM challenge_result WHERE challenge_id = ? " +
                "ORDER BY score DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LeaderboardEntry e = new LeaderboardEntry();
                e.setId         (rs.getInt      ("id"));
                e.setScore      (rs.getInt      ("score"));
                e.setBadges     (rs.getString   ("badges"));
                e.setCreatedAt  (rs.getTimestamp("created_at"));
                e.setReponses   (rs.getString   ("reponses"));
                e.setUserId     (rs.getInt      ("user_id"));
                e.setChallengeId(rs.getInt      ("challenge_id"));
                return e;
            }
        }
        return null;
    }

    // ── Supprimer tous les scores d'un challenge ──────────────
    public void supprimerParChallenge(int challengeId) throws SQLException {
        String sql = "DELETE FROM challenge_result WHERE challenge_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ps.executeUpdate();
        }
    }

    // ── Supprimer un score par id ─────────────────────────────
    public void supprimerParId(int id) throws SQLException {
        String sql = "DELETE FROM challenge_result WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Compter les participations d'un challenge ─────────────
    public int compterParticipations(int challengeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM challenge_result WHERE challenge_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Score moyen d'un challenge ────────────────────────────
    public double getScoreMoyen(int challengeId) throws SQLException {
        String sql = "SELECT AVG(score) FROM challenge_result WHERE challenge_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }
}