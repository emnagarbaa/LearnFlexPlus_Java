package org.example.Services;

import org.example.entities.Cours;
import org.example.entities.Matiere;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours implements Iservice<Cours> {
//Sert à récupérer les infos de la Matière liée au cours
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    // Valide que toutes les informations du cours sont cohérentes et présentes avant l'action en base
    private void valider(Cours c) {
        if (c.getTitre() == null || c.getTitre().isBlank())
            throw new IllegalArgumentException("Le titre du cours est obligatoire.");
        if (c.getTitre().length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        if (c.getDescription() == null || c.getDescription().isBlank())
            throw new IllegalArgumentException("La description est obligatoire.");
        if (c.getSection() == null || c.getSection().isBlank())
            throw new IllegalArgumentException("La section est obligatoire.");
        if (c.getDureeTotale() == null || c.getDureeTotale().isBlank())
            throw new IllegalArgumentException("La durée totale est obligatoire.");
        if (c.getLangue() == null || c.getLangue().isBlank())
            throw new IllegalArgumentException("La langue est obligatoire.");
        if (c.getMatiereId() <= 0)
            throw new IllegalArgumentException("Veuillez sélectionner une matière.");
        if (c.getPrix() < 0)
            throw new IllegalArgumentException("Le prix doit être positif ou zéro.");

        // Sécurité contre les injections XSS
        verifierContenuDangereux(c.getTitre(), "titre");
        verifierContenuDangereux(c.getDescription(), "description");
    }

    // Détecte les tentatives d'insertion de code malveillant dans les champs de saisie
    private void verifierContenuDangereux(String valeur, String champ) {
        if (valeur == null) return;
        String v = valeur.toLowerCase();
        if (v.contains("<script") || v.contains("javascript:") || v.contains("onerror="))
            throw new IllegalArgumentException("Contenu dangereux détecté dans le champ : " + champ);
    }

    // Enregistre un nouveau cours dans la table 'cours' et initialise sa date de création
    @Override
    public void ajouter(Cours cours) throws SQLException {
        valider(cours);
//Singleton qui donne la connexion à la base de données.
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "INSERT INTO cours (titre, description, date_creation, section, duree_totale, langue, image, pdf_file, matiere_id, prix) " +
                "VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, cours.getTitre().trim());
        ps.setString(2, cours.getDescription().trim());
        ps.setString(3, cours.getSection().trim());
        ps.setString(4, cours.getDureeTotale().trim());
        ps.setString(5, cours.getLangue().trim());
        ps.setString(6, cours.getImage());
        ps.setString(7, cours.getPdfFile());
        ps.setInt(8, cours.getMatiereId());
        ps.setDouble(9, cours.getPrix());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            cours.setId(rs.getInt(1));
        }
        System.out.println("✅ Cours ajouté !");
    }

    // Modifie les données d'un cours existant en utilisant son identifiant SQL
    @Override
    public void modifier(Cours cours) throws SQLException {
        if (cours.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");
        valider(cours);

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "UPDATE cours SET titre=?, description=?, section=?, duree_totale=?, langue=?, image=?, pdf_file=?, matiere_id=?, prix=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, cours.getTitre().trim());
        ps.setString(2, cours.getDescription().trim());
        ps.setString(3, cours.getSection().trim());
        ps.setString(4, cours.getDureeTotale().trim());
        ps.setString(5, cours.getLangue().trim());
        ps.setString(6, cours.getImage());
        ps.setString(7, cours.getPdfFile());
        ps.setInt(8, cours.getMatiereId());
        ps.setDouble(9, cours.getPrix());
        ps.setInt(10, cours.getId());
        ps.executeUpdate();
        System.out.println("✅ Cours modifié !");
    }

    // Retire un cours de la base de données de manière permanente
    @Override
    public void supprimer(Cours cours) throws SQLException {
        if (cours == null || cours.getId() <= 0)
            throw new IllegalArgumentException("Cours invalide ou ID manquant.");

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "DELETE FROM cours WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, cours.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Cours supprimé !");
        else          System.out.println("⚠️ Aucun cours trouvé avec cet ID.");
    }

    // Récupère l'intégralité des cours de la plateforme dans une liste d'objets
    @Override
    public List<Cours> recuperer() throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours ORDER BY date_creation DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapCours(rs));
        }
        return list;
    }

    // Récupère uniquement les cours appartenant à une matière spécifique
    public List<Cours> recupererParMatiere(int matiereId) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE matiere_id=? ORDER BY date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, matiereId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapCours(rs));
        }
        return list;
    }

    // Trouve et retourne un seul cours en cherchant par son ID
    public Cours recupererParId(int id) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "SELECT * FROM cours WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapCours(rs);
        }
        return null;
    }

    // Transforme une ligne de résultat SQL (ResultSet) en un objet de type 'Cours'
    private Cours mapCours(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setSection(rs.getString("section"));
        c.setDureeTotale(rs.getString("duree_totale"));
        c.setLangue(rs.getString("langue"));
        c.setImage(rs.getString("image"));
        c.setPdfFile(rs.getString("pdf_file"));
        c.setMatiereId(rs.getInt("matiere_id"));
        c.setPrix(rs.getDouble("prix"));

        // Charge la référence vers l'objet Matière pour faciliter l'affichage
        try {
            Matiere m = serviceMatiere.recupererParId(c.getMatiereId());
            c.setMatiere(m);
        } catch (Exception ignored) {}

        return c;
    }
}
