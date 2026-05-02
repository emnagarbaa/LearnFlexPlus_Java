package org.example.Services;

import org.example.entities.Examen;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceExamen implements Iservice<Examen> {

    private Connection connection;

    private static final Set<String> NIVEAUX = Set.of("facile", "moyen", "difficile");
    private static final Set<String> TYPES   = Set.of("QCM", "Redaction", "QCM+Redac.", "Oral", "Pratique");
    private static final Set<String> ETATS   = Set.of("Actif", "En attente", "Termine");

    public ServiceExamen() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    private void valider(Examen e) {

        // 1. Champs obligatoires
        if (e.getTitre() == null || e.getTitre().isBlank())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (e.getMatiere() == null || e.getMatiere().isBlank())
            throw new IllegalArgumentException("La matière est obligatoire.");
        if (e.getNiveauexamen() == null || e.getNiveauexamen().isBlank())
            throw new IllegalArgumentException("Le niveau est obligatoire.");
        if (e.getTypeexamen() == null || e.getTypeexamen().isBlank())
            throw new IllegalArgumentException("Le type d'examen est obligatoire.");
        if (e.getEtat() == null || e.getEtat().isBlank())
            throw new IllegalArgumentException("L'état est obligatoire.");
        if (e.getDatedebut() == null)
            throw new IllegalArgumentException("La date de début est obligatoire.");
        if (e.getDatefin() == null)
            throw new IllegalArgumentException("La date de fin est obligatoire.");

        // 2. Longueurs maximales
        if (e.getTitre().length() > 150)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 150 caractères.");
        if (e.getMatiere().length() > 100)
            throw new IllegalArgumentException("La matière ne doit pas dépasser 100 caractères.");
        if (e.getDescription() != null && e.getDescription().length() > 2000)
            throw new IllegalArgumentException("La description ne doit pas dépasser 2000 caractères.");
        if (e.getPdf() != null && e.getPdf().length() > 500)
            throw new IllegalArgumentException("Le chemin PDF ne doit pas dépasser 500 caractères.");

        // 3. Valeurs autorisées (enum)
        if (!NIVEAUX.contains(e.getNiveauexamen()))
            throw new IllegalArgumentException("Niveau invalide : " + e.getNiveauexamen());
        if (!TYPES.contains(e.getTypeexamen()))
            throw new IllegalArgumentException("Type d'examen invalide : " + e.getTypeexamen());
        if (!ETATS.contains(e.getEtat()))
            throw new IllegalArgumentException("État invalide : " + e.getEtat());

        // 4. Cohérence des dates
        LocalDate debut = e.getDatedebut().toLocalDate();
        LocalDate fin   = e.getDatefin().toLocalDate();
        if (!fin.isAfter(debut))
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");

        // 5. Contraintes numériques
        if (e.getDuree() < 1)
            throw new IllegalArgumentException("La durée doit être d'au moins 1 minute.");
        if (e.getNbquestion() < 1)
            throw new IllegalArgumentException("Le nombre de questions doit être d'au moins 1.");
        if (e.getScoretotal() <= 0)
            throw new IllegalArgumentException("Le score total doit être supérieur à 0.");
        if (e.getCoefficient() <= 0)
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");

        // 6. Sécurité : pas de balises HTML/script dans les champs texte
        verifierContenuDangereux(e.getTitre(),       "titre");
        verifierContenuDangereux(e.getDescription(), "description");
        verifierContenuDangereux(e.getMatiere(),     "matiere");
    }

    private void verifierContenuDangereux(String valeur, String champ) {
        if (valeur == null) return;
        String v = valeur.toLowerCase();
        if (v.contains("<script") || v.contains("javascript:") || v.contains("onerror="))
            throw new IllegalArgumentException("Contenu dangereux détecté dans le champ : " + champ);
    }

    // ══════════════════════════════════════════════
    //  AJOUTER
    // ══════════════════════════════════════════════
    @Override
    public void ajouter(Examen examen) throws SQLException {
        valider(examen);  // ← validation avant tout

        String sql = "INSERT INTO examen (titre, description, matiere, niveauexamen, datedebut, datefin, " +
                "duree, nbquestion, scoretotal, coefficient, typeexamen, etat, pdf, questions, " +
                "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, examen.getTitre().trim());
        ps.setString(2, examen.getDescription() != null ? examen.getDescription().trim() : null);
        ps.setString(3, examen.getMatiere().trim());
        ps.setString(4, examen.getNiveauexamen());
        ps.setDate(5, new java.sql.Date(examen.getDatedebut().getTime()));
        ps.setDate(6, new java.sql.Date(examen.getDatefin().getTime()));
        ps.setInt(7, examen.getDuree());
        ps.setInt(8, examen.getNbquestion());
        ps.setDouble(9, examen.getScoretotal());
        ps.setDouble(10, examen.getCoefficient());
        ps.setString(11, examen.getTypeexamen());
        ps.setString(12, examen.getEtat());
        ps.setString(13, examen.getPdf() != null ? examen.getPdf().trim() : null);
        ps.setString(14, examen.getQuestions());

        ps.executeUpdate();
        System.out.println("✅ Examen ajouté !");
    }

    // ══════════════════════════════════════════════
    //  MODIFIER
    // ══════════════════════════════════════════════
    @Override
    public void modifier(Examen examen) throws SQLException {
        if (examen.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");

        valider(examen);  // ← validation avant tout

        String sql = "UPDATE examen SET titre=?, description=?, matiere=?, niveauexamen=?, " +
                "datedebut=?, datefin=?, duree=?, nbquestion=?, scoretotal=?, coefficient=?, " +
                "typeexamen=?, etat=?, pdf=?, questions=?, updated_at=NOW() WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, examen.getTitre().trim());
        ps.setString(2, examen.getDescription() != null ? examen.getDescription().trim() : null);
        ps.setString(3, examen.getMatiere().trim());
        ps.setString(4, examen.getNiveauexamen());
        ps.setDate(5, new java.sql.Date(examen.getDatedebut().getTime()));
        ps.setDate(6, new java.sql.Date(examen.getDatefin().getTime()));
        ps.setInt(7, examen.getDuree());
        ps.setInt(8, examen.getNbquestion());
        ps.setDouble(9, examen.getScoretotal());
        ps.setDouble(10, examen.getCoefficient());
        ps.setString(11, examen.getTypeexamen());
        ps.setString(12, examen.getEtat());
        ps.setString(13, examen.getPdf() != null ? examen.getPdf().trim() : null);
        ps.setString(14, examen.getQuestions());
        ps.setInt(15, examen.getId());

        ps.executeUpdate();
        System.out.println("✅ Examen modifié !");
    }

    // ══════════════════════════════════════════════
    //  SUPPRIMER
    // ══════════════════════════════════════════════
    @Override
    public void supprimer(Examen examen) throws SQLException {
        if (examen == null || examen.getId() <= 0)
            throw new IllegalArgumentException("Examen invalide ou ID manquant pour la suppression.");

        String sqlChallenge   = "DELETE FROM challenge WHERE examen_id = ?";
        String sqlCommentaire = "DELETE FROM commentaire WHERE examen_id = ?";
        String sqlExamen      = "DELETE FROM examen WHERE id = ?";

        PreparedStatement ps1 = connection.prepareStatement(sqlChallenge);
        ps1.setInt(1, examen.getId());
        ps1.executeUpdate();

        PreparedStatement ps2 = connection.prepareStatement(sqlCommentaire);
        ps2.setInt(1, examen.getId());
        ps2.executeUpdate();

        PreparedStatement ps = connection.prepareStatement(sqlExamen);
        ps.setInt(1, examen.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Examen supprimé !");
        else          System.out.println("⚠️ Aucun examen trouvé avec cet ID.");
    }

    // ══════════════════════════════════════════════
    //  RÉCUPÉRER
    // ══════════════════════════════════════════════
    @Override
    public List<Examen> recuperer() throws SQLException {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen";
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
        return examens;
    }
}
