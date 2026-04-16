package org.example.Services;

import org.example.entities.Organisme;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganismeService {

    private final Connection conn;

    public OrganismeService() {
        this.conn = MyDatabase.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────
    public void create(Organisme o) throws SQLException {
        String sql = "INSERT INTO organisme (nom, type, description, siteWeb, email, " +
                "telephone, ville, actif, fraisMin, langue, " +
                "opportunitesStage, opportunitesEmploi, photo) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  o.getNom());
            ps.setString(2,  o.getType());
            ps.setString(3,  o.getDescription());
            ps.setString(4,  o.getSiteWeb());
            ps.setString(5,  o.getEmail());
            ps.setString(6,  o.getTelephone());
            ps.setString(7,  o.getVille());
            ps.setBoolean(8, o.isActif());
            ps.setDouble(9,  o.getFraisMin());
            ps.setString(10, o.getLangue());
            ps.setBoolean(11,o.isOpportunitesStage());
            ps.setBoolean(12,o.isOpportunitesEmploi());
            ps.setString(13, o.getPhoto());
            ps.executeUpdate();
        }
    }

    // ── READ ALL ──────────────────────────────────────────────────────
    public List<Organisme> findAll() throws SQLException {
        return runQuery("SELECT * FROM organisme ORDER BY id");
    }

    // ── SEARCH ────────────────────────────────────────────────────────
    public List<Organisme> search(String keyword) throws SQLException {
        List<Organisme> list = new ArrayList<>();
        String sql = "SELECT * FROM organisme WHERE nom LIKE ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── SORT by fraisMin ──────────────────────────────────────────────
    public List<Organisme> sortByFrais(boolean asc) throws SQLException {
        return runQuery("SELECT * FROM organisme ORDER BY fraisMin " + (asc ? "ASC" : "DESC"));
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    public void update(Organisme o) throws SQLException {
        String sql = "UPDATE organisme SET nom=?, type=?, description=?, siteWeb=?, " +
                "email=?, telephone=?, ville=?, actif=?, fraisMin=?, langue=?, " +
                "opportunitesStage=?, opportunitesEmploi=?, photo=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  o.getNom());
            ps.setString(2,  o.getType());
            ps.setString(3,  o.getDescription());
            ps.setString(4,  o.getSiteWeb());
            ps.setString(5,  o.getEmail());
            ps.setString(6,  o.getTelephone());
            ps.setString(7,  o.getVille());
            ps.setBoolean(8, o.isActif());
            ps.setDouble(9,  o.getFraisMin());
            ps.setString(10, o.getLangue());
            ps.setBoolean(11,o.isOpportunitesStage());
            ps.setBoolean(12,o.isOpportunitesEmploi());
            ps.setString(13, o.getPhoto());
            ps.setInt(14,    o.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM organisme WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────
    private List<Organisme> runQuery(String sql) throws SQLException {
        List<Organisme> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Organisme map(ResultSet rs) throws SQLException {
        return new Organisme(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getString("siteWeb"),
                rs.getString("email"),
                rs.getString("telephone"),
                rs.getString("ville"),
                rs.getBoolean("actif"),
                rs.getDouble("fraisMin"),
                rs.getString("langue"),
                rs.getBoolean("opportunitesStage"),
                rs.getBoolean("opportunitesEmploi"),
                rs.getString("photo")
        );
    }
}