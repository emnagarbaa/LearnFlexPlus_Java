package org.example.Services;

import org.example.entities.Challenge;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceChallenge implements Iservice<Challenge> {

    private static final Set<String> NIVEAUX = Set.of("Facile", "Moyen", "Difficile");
    private static final Set<String> ETATS   = Set.of("Actif", "En attente", "Termine");

    // ══════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════
    private void valider(Challenge c) {

        // 1. Champs obligatoires
        if (c.getTitrec() == null || c.getTitrec().isBlank())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (c.getNiveaudifficulte() == null || c.getNiveaudifficulte().isBlank())
            throw new IllegalArgumentException("Le niveau de difficulté est obligatoire.");
        if (c.getEtat() == null || c.getEtat().isBlank())
            throw new IllegalArgumentException("L'état est obligatoire.");
        if (c.getExamen_id() <= 0)
            throw new IllegalArgumentException("Un examen valide est obligatoire.");

        // 2. Longueurs maximales
        if (c.getTitrec().length() > 150)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 150 caractères.");
        if (c.getDescriptionc() != null && c.getDescriptionc().length() > 2000)
            throw new IllegalArgumentException("La description ne doit pas dépasser 2000 caractères.");
        if (c.getTyperecomponse() != null && c.getTyperecomponse().length() > 100)
            throw new IllegalArgumentException("Le type de récompense ne doit pas dépasser 100 caractères.");
        if (c.getContenurecompense() != null && c.getContenurecompense().length() > 500)
            throw new IllegalArgumentException("Le contenu de récompense ne doit pas dépasser 500 caractères.");

        // 3. Valeurs autorisées (enum)
        if (!NIVEAUX.contains(c.getNiveaudifficulte()))
            throw new IllegalArgumentException("Niveau invalide : " + c.getNiveaudifficulte());
        if (!ETATS.contains(c.getEtat()))
            throw new IllegalArgumentException("État invalide : " + c.getEtat());

        // 4. Contraintes numériques
        if (c.getObjectifscore() <= 0)
            throw new IllegalArgumentException("L'objectif de score doit être supérieur à 0.");
        if (c.getProgressionactuelle() < 0)
            throw new IllegalArgumentException("La progression ne peut pas être négative.");
        if (c.getProgressionactuelle() > c.getObjectifscore())
            throw new IllegalArgumentException("La progression ne peut pas dépasser l'objectif de score.");
        if (c.getDernier_score() < 0)
            throw new IllegalArgumentException("Le dernier score ne peut pas être négatif.");

        // 5. Cohérence des dates (si renseignées)
        if (c.getDated() != null && c.getDatef() != null) {
            LocalDate debut = c.getDated().toLocalDate();
            LocalDate fin   = c.getDatef().toLocalDate();
            if (!fin.isAfter(debut))
                throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        if (c.getDatelimite() != null && c.getDated() != null) {
            if (c.getDatelimite().toLocalDate().isBefore(c.getDated().toLocalDate()))
                throw new IllegalArgumentException("La date limite ne peut pas être avant la date de début.");
        }

        // 6. Sécurité : pas de balises script
        verifierContenuDangereux(c.getTitrec(),        "titre");
        verifierContenuDangereux(c.getDescriptionc(),  "description");
        verifierContenuDangereux(c.getQuestion(),      "question");
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
    public void ajouter(Challenge challenge) throws SQLException {
        valider(challenge);  // ← validation avant tout

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "INSERT INTO challenge (titrec, descriptionc, objectifscore, progressionactuelle, " +
                "niveaudifficulte, niveauatteint, typerecomponse, contenurecompense, etat, " +
                "dated, datef, datelimite, alerte, question, images, dernier_score, " +
                "dernier_niveau, reponses, interacty_hash, created_at, updated_at, examen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, challenge.getTitrec().trim());
        ps.setString(2, challenge.getDescriptionc() != null ? challenge.getDescriptionc().trim() : null);
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

    // ══════════════════════════════════════════════
    //  MODIFIER
    // ══════════════════════════════════════════════
    @Override
    public void modifier(Challenge challenge) throws SQLException {
        if (challenge.getId() <= 0)
            throw new IllegalArgumentException("ID invalide pour la modification.");

        valider(challenge);  // ← validation avant tout

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "UPDATE challenge SET titrec=?, descriptionc=?, objectifscore=?, progressionactuelle=?, " +
                "niveaudifficulte=?, niveauatteint=?, typerecomponse=?, contenurecompense=?, etat=?, " +
                "dated=?, datef=?, datelimite=?, alerte=?, question=?, images=?, dernier_score=?, " +
                "dernier_niveau=?, reponses=?, interacty_hash=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, challenge.getTitrec().trim());
        ps.setString(2, challenge.getDescriptionc() != null ? challenge.getDescriptionc().trim() : null);
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
    }

    // ══════════════════════════════════════════════
    //  SUPPRIMER
    // ══════════════════════════════════════════════
    @Override
    public void supprimer(Challenge challenge) throws SQLException {
        if (challenge == null || challenge.getId() <= 0)
            throw new IllegalArgumentException("Challenge invalide ou ID manquant.");

        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "DELETE FROM challenge WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, challenge.getId());
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("✅ Challenge supprimé !");
        else          System.out.println("⚠️ Aucun challenge trouvé avec cet ID.");
    }

    // ══════════════════════════════════════════════
    //  RÉCUPÉRER
    // ══════════════════════════════════════════════
    @Override
    public List<Challenge> recuperer() throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        List<Challenge> list = new ArrayList<>();
        String sql = "SELECT * FROM challenge";
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
        return list;
    }

    public void supprimerParExamenId(int examenId) throws SQLException {
        Connection connection = MyDatabase.getInstance().getConnection();
        String sql = "DELETE FROM challenge WHERE examen_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, examenId);
        int rows = ps.executeUpdate();
        System.out.println("✅ " + rows + " challenge(s) supprimé(s) !");
    }
}