package org.example.Services;

import org.example.entities.Matiere;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatiere implements Iservice<Matiere> {

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    private void valider(Matiere m) {
        if (m.getNomMatiere() == null || m.getNomMatiere().isBlank())
            throw new IllegalArgumentException("Le nom de la matière est obligatoire.");
        if (m.getNomMatiere().length() < 3)
            throw new IllegalArgumentException("Le nom doit contenir au moins 3 caractères.");
        if (m.getNomMatiere().length() > 255)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 255 caractères.");
        if (m.getDescription() == null || m.getDescription().isBlank())
            throw new IllegalArgumentException("La description est obligatoire.");
        if (m.getSection() == null || m.getSection().isBlank())
            throw new IllegalArgumentException("La section est obligatoire.");
        if (m.getCodeMatiere() == null || m.getCodeMatiere().isBlank())
            throw new IllegalArgumentException("Le code de la matière est obligatoire.");
        if (m.getNiveau() == null || m.getNiveau().isBlank())
            throw new IllegalArgumentException("Le niveau est obligatoire.");

        verifierContenuDangereux(m.getNomMatiere(), "nom");
        verifierContenuDangereux(m.getDescription(), "description");
    }

    private void verifierContenuDangereux(String valeur, String champ) {
        if (valeur == null) return;
        String v = valeur.toLowerCase();
        if (v.contains("<script") || v.contains("javascript:") || v.contains("onerror="))
            throw new IllegalArgumentException("Contenu dangereux détecté dans le champ : " + champ);
    }

    @Override
    public void ajouter(Matiere matiere) throws SQLException {
        valider(matiere);
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "INSERT INTO matiere (nom_matiere, description, section, code_matiere, date_creation, niveau, image) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, matiere.getNomMatiere().trim());
        ps.setString(2, matiere.getDescription().trim());
        ps.setString(3, matiere.getSection().trim());
        ps.setString(4, matiere.getCodeMatiere().trim());
        ps.setString(5, matiere.getNiveau().trim());
        ps.setString(6, matiere.getImage());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            matiere.setId(rs.getInt(1));
        }
        System.out.println("✅ Matière ajoutée !");
    }

    @Override
    public void modifier(Matiere matiere) throws SQLException {
        if (matiere.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");
        valider(matiere);

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "UPDATE matiere SET nom_matiere=?, description=?, section=?, code_matiere=?, niveau=?, image=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, matiere.getNomMatiere().trim());
        ps.setString(2, matiere.getDescription().trim());
        ps.setString(3, matiere.getSection().trim());
        ps.setString(4, matiere.getCodeMatiere().trim());
        ps.setString(5, matiere.getNiveau().trim());
        ps.setString(6, matiere.getImage());
        ps.setInt(7, matiere.getId());
        ps.executeUpdate();
        System.out.println("✅ Matière modifiée !");
    }

    @Override
    public void supprimer(Matiere matiere) throws SQLException {
        if (matiere == null || matiere.getId() <= 0)
            throw new IllegalArgumentException("Matière invalide ou ID manquant.");

        Connection connection = MyDatabase.getInstance().getConnection();
        // Supprimer les cours liés d'abord (intégrité référentielle)
        PreparedStatement psCours = connection.prepareStatement("DELETE FROM cours WHERE matiere_id=?");
        psCours.setInt(1, matiere.getId());
        psCours.executeUpdate();

        PreparedStatement ps = connection.prepareStatement("DELETE FROM matiere WHERE id=?");
        ps.setInt(1, matiere.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Matière supprimée !");
        else          System.out.println("⚠️ Aucune matière trouvée avec cet ID.");
    }

    @Override
    public List<Matiere> recuperer() throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        List<Matiere> list = new ArrayList<>();
        ResultSet rs = connection.createStatement()
                .executeQuery("SELECT * FROM matiere ORDER BY date_creation DESC");
        while (rs.next()) {
            Matiere m = new Matiere();
            m.setId(rs.getInt("id"));
            m.setNomMatiere(rs.getString("nom_matiere"));
            m.setDescription(rs.getString("description"));
            m.setSection(rs.getString("section"));
            m.setCodeMatiere(rs.getString("code_matiere"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setNiveau(rs.getString("niveau"));
            m.setImage(rs.getString("image"));
            list.add(m);
        }
        return list;
    }

    public Matiere recupererParId(int id) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM matiere WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Matiere m = new Matiere();
            m.setId(rs.getInt("id"));
            m.setNomMatiere(rs.getString("nom_matiere"));
            m.setDescription(rs.getString("description"));
            m.setSection(rs.getString("section"));
            m.setCodeMatiere(rs.getString("code_matiere"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setNiveau(rs.getString("niveau"));
            m.setImage(rs.getString("image"));
            return m;
        }
        return null;
    }

    public int compterCours(int matiereId) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM cours WHERE matiere_id=?");
        ps.setInt(1, matiereId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}
