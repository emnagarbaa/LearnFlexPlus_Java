package org.example.Services;

import org.example.entities.Evenement;
import org.example.entities.Organisme;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {

    private final Connection conn;

    public EvenementService() {
        this.conn = MyDatabase.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────
    public void create(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenement (titre, description, dateDebut, dateFin, " +
                "lieu, mode, capaciteMax, publicCible, organisme_id, " +
                "contactEmail, contactTelephone, lienInscription) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  e.getTitre());
            ps.setString(2,  e.getDescription());
            ps.setTimestamp(3, e.getDateDebut() != null ? Timestamp.valueOf(e.getDateDebut()) : null);
            ps.setTimestamp(4, e.getDateFin()   != null ? Timestamp.valueOf(e.getDateFin())   : null);
            ps.setString(5,  e.getLieu());
            ps.setString(6,  e.getMode());
            ps.setInt(7,     e.getCapaciteMax());
            ps.setString(8,  e.getPublicCible());
            if (e.getOrganisme() != null) ps.setInt(9, e.getOrganisme().getId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getContactEmail());
            ps.setString(11, e.getContactTelephone());
            ps.setString(12, e.getLienInscription());
            ps.executeUpdate();
        }
    }
//récupérer tous les événements de la base de données.
    public List<Evenement> findAll() throws SQLException {
        return runQuery(
                "SELECT e.*, o.nom as orgNom FROM evenement e " +
                        "LEFT JOIN organisme o ON e.organisme_id = o.id ORDER BY e.id");
    }
//rs.next() avance ligne par ligne. map(rs) convertit chaque ligne SQL en objet Evenement Java et l'ajoute à la liste.
    // ── SEARCH by titre ───────────────────────────────────────────────
    public List<Evenement> search(String keyword) throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT e.*, o.nom as orgNom FROM evenement e " +
                "LEFT JOIN organisme o ON e.organisme_id = o.id " +
                "WHERE e.titre LIKE ? ORDER BY e.id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── SORT by capaciteMax ───────────────────────────────────────────
    public List<Evenement> sortByCapacite(boolean asc) throws SQLException {
        return runQuery(
                "SELECT e.*, o.nom as orgNom FROM evenement e " +
                        "LEFT JOIN organisme o ON e.organisme_id = o.id " +
                        "ORDER BY e.capaciteMax " + (asc ? "ASC" : "DESC"));
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    public void update(Evenement e) throws SQLException {
        String sql = "UPDATE evenement SET titre=?, description=?, dateDebut=?, dateFin=?, " +
                "lieu=?, mode=?, capaciteMax=?, publicCible=?, organisme_id=?, " +
                "contactEmail=?, contactTelephone=?, lienInscription=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  e.getTitre());
            ps.setString(2,  e.getDescription());
            ps.setTimestamp(3, e.getDateDebut() != null ? Timestamp.valueOf(e.getDateDebut()) : null);
            ps.setTimestamp(4, e.getDateFin()   != null ? Timestamp.valueOf(e.getDateFin())   : null);
            ps.setString(5,  e.getLieu());
            ps.setString(6,  e.getMode());
            ps.setInt(7,     e.getCapaciteMax());
            ps.setString(8,  e.getPublicCible());
            if (e.getOrganisme() != null) ps.setInt(9, e.getOrganisme().getId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getContactEmail());
            ps.setString(11, e.getContactTelephone());
            ps.setString(12, e.getLienInscription());
            ps.setInt(13,    e.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM evenement WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────
    private List<Evenement> runQuery(String sql) throws SQLException {
        List<Evenement> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Evenement map(ResultSet rs) throws SQLException {
        Organisme org = null;
        int orgId = rs.getInt("organisme_id");
        if (!rs.wasNull()) {
            org = new Organisme();
            org.setId(orgId);
            org.setNom(rs.getString("orgNom"));
        }

        Timestamp tsDebut = rs.getTimestamp("dateDebut");
        Timestamp tsFin   = rs.getTimestamp("dateFin");

        return new Evenement(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                tsDebut != null ? tsDebut.toLocalDateTime() : null,
                tsFin   != null ? tsFin.toLocalDateTime()   : null,
                rs.getString("lieu"),
                rs.getString("mode"),
                rs.getInt("capaciteMax"),
                rs.getString("publicCible"),
                org,
                rs.getString("contactEmail"),
                rs.getString("contactTelephone"),
                rs.getString("lienInscription")
        );
    }
}
