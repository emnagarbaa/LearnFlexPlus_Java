package org.example.Services;
import entities.Users;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class ServiceUsers implements Iservice<Users> {

    // VALIDATION
    private void validate(Users u) {

        // NOM
        if (u.getNom() == null || u.getNom().isBlank())
            throw new IllegalArgumentException("Le nom est obligatoire.");

        if (u.getNom().length() < 2)
            throw new IllegalArgumentException("Le nom doit contenir au moins 2 caractères.");

        if (!u.getNom().matches("^[a-zA-Z\\s]+$"))
            throw new IllegalArgumentException("Le nom ne peut contenir que des lettres et des espaces.");

        // PRENOM
        if (u.getPrenom() == null || u.getPrenom().isBlank())
            throw new IllegalArgumentException("Le prénom est obligatoire.");

        if (u.getPrenom().length() < 2)
            throw new IllegalArgumentException("Le prénom doit contenir au moins 2 caractères.");

        if (!u.getPrenom().matches("^[a-zA-Z\\s]+$"))
            throw new IllegalArgumentException("Le prénom ne peut contenir que des lettres et des espaces.");

        // AGE
        if (u.getAge() != null) {
            if (u.getAge() < 18)
                throw new IllegalArgumentException("Vous devez avoir au moins 18 ans.");
        }

        // ADRESSE
        if (u.getAdresseResidence() != null && !u.getAdresseResidence().isBlank()) {
            if (u.getAdresseResidence().length() > 255)
                throw new IllegalArgumentException("L'adresse ne peut pas dépasser 255 caractères.");

            if (!u.getAdresseResidence().matches("^\\d+\\s*,\\s*[a-zA-Z\\s]+,\\s*[a-zA-Z\\s]+$"))
                throw new IllegalArgumentException(
                        "L'adresse doit être au format: Numéro, Rue/Avenue, Gouvernorat"
                );
        }

        // EMAIL
        if (u.getEmail() == null || u.getEmail().isBlank())
            throw new IllegalArgumentException("L'email est obligatoire.");

        if (!u.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Email invalide.");

        // TELEPHONE
        if (u.getTelephone() != null && !u.getTelephone().isBlank()) {
            if (!u.getTelephone().matches("^\\+?[0-9\\s\\-]+$"))
                throw new IllegalArgumentException("Numéro de téléphone invalide.");
        }

        // PASSWORD (registration only)
        if (u.getPassword() == null || u.getPassword().isBlank())
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");

        if (u.getPassword().length() < 8)
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");

        if (!u.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$"))
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir majuscule, minuscule, chiffre et caractère spécial."
            );

        // ROLE
        if (u.getRole() == null || u.getRole().isBlank())
            throw new IllegalArgumentException("Le rôle est obligatoire.");

        if (!u.getRole().matches("ADMIN|STUDENT|TEACHER|Admin|Etudiant|Enseignant"))
            throw new IllegalArgumentException("Rôle invalide.");
    }
    private void validateUpdate(Users u) {

        validateCommon(u);

        // password optional in update
        if (u.getPassword() != null && !u.getPassword().isBlank()) {

            if (u.getPassword().length() < 8)
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");

            if (!u.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$"))
                throw new IllegalArgumentException(
                        "Mot de passe invalide (complexité requise)."
                );
        }
    }

    private void validateCommon(Users u) {

        // NOM
        if (u.getNom() == null || u.getNom().isBlank())
            throw new IllegalArgumentException("Le nom est obligatoire.");

        if (u.getNom().length() < 2)
            throw new IllegalArgumentException("Le nom doit contenir au moins 2 caractères.");

        if (!u.getNom().matches("^[a-zA-Z\\s]+$"))
            throw new IllegalArgumentException("Nom invalide (lettres uniquement).");

        // PRENOM
        if (u.getPrenom() == null || u.getPrenom().isBlank())
            throw new IllegalArgumentException("Le prénom est obligatoire.");

        if (u.getPrenom().length() < 2)
            throw new IllegalArgumentException("Le prénom doit contenir au moins 2 caractères.");

        if (!u.getPrenom().matches("^[a-zA-Z\\s]+$"))
            throw new IllegalArgumentException("Prénom invalide.");

        // AGE
        if (u.getAge() != null && u.getAge() < 18)
            throw new IllegalArgumentException("Vous devez avoir au moins 18 ans.");

        // EMAIL
        if (u.getEmail() == null || u.getEmail().isBlank())
            throw new IllegalArgumentException("L'email est obligatoire.");

        if (!u.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Email invalide.");

        // TELEPHONE
        if (u.getTelephone() != null && !u.getTelephone().isBlank()) {
            if (!u.getTelephone().matches("^\\+?[0-9\\s\\-]+$"))
                throw new IllegalArgumentException("Téléphone invalide.");
        }

        // ADRESSE
        if (u.getAdresseResidence() != null && !u.getAdresseResidence().isBlank()) {

            if (u.getAdresseResidence().length() > 255)
                throw new IllegalArgumentException("Adresse trop longue.");

            if (!u.getAdresseResidence().matches("^\\d+\\s*,\\s*[a-zA-Z\\s]+,\\s*[a-zA-Z\\s]+$"))
                throw new IllegalArgumentException("Format adresse invalide.");
        }

        // ROLE
        if (u.getRole() == null || u.getRole().isBlank())
            throw new IllegalArgumentException("Le rôle est obligatoire.");

        if (!u.getRole().matches("ADMIN|STUDENT|TEACHER|Admin|Etudiant|Enseignant"))
            throw new IllegalArgumentException("Rôle invalide.");
    }


    // ADD
    @Override
    public void ajouter(Users u) throws SQLException {

        validate(u);

        String sql = """
            INSERT INTO users
            (nom, prenom, age, adresse_residence, email, telephone,
             password, role, created_at, updated_at, is_verified, profile_image)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)
        """;

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setObject(3, u.getAge());
        ps.setString(4, u.getAdresseResidence());
        ps.setString(5, u.getEmail());
        ps.setString(6, u.getTelephone());
        String hashedPassword = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());
        ps.setString(7, hashedPassword);
        ps.setString(8, u.getRole());
        ps.setBoolean(9, u.isVerified());
        ps.setString(10, u.getProfileImage());

        ps.executeUpdate();
    }

    // UPDATE
    @Override
    public void modifier(Users u) throws SQLException {

        if (u.getId() <= 0)
            throw new IllegalArgumentException("ID invalide");

        validate(u);

        String sql = """
            UPDATE users SET
            nom=?, prenom=?, age=?, adresse_residence=?,
            email=?, telephone=?, password=?, role=?,
            updated_at=NOW(), is_verified=?, profile_image=?
            WHERE id=?
        """;

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setObject(3, u.getAge());
        ps.setString(4, u.getAdresseResidence());
        ps.setString(5, u.getEmail());
        ps.setString(6, u.getTelephone());
        ps.setString(7, u.getPassword());
        ps.setString(8, u.getRole());
        ps.setBoolean(9, u.isVerified());
        ps.setString(10, u.getProfileImage());
        ps.setInt(11, u.getId());

        ps.executeUpdate();
    }

    // DELETE
    @Override
    public void supprimer(Users u) throws SQLException {

        String sql = "DELETE FROM users WHERE id=?";

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, u.getId());

        ps.executeUpdate();
    }

    // GET ALL
    @Override
    public List<Users> recuperer() throws SQLException {

        List<Users> list = new ArrayList<>();

        String sql = "SELECT * FROM users";

        Connection conn = MyDatabase.getInstance().getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {

            Users u = new Users();

            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setAge(rs.getInt("age"));
            u.setAdresseResidence(rs.getString("adresse_residence"));
            u.setEmail(rs.getString("email"));
            u.setTelephone(rs.getString("telephone"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getString("role"));
            u.setCreatedAt(rs.getTimestamp("created_at"));
            u.setUpdatedAt(rs.getTimestamp("updated_at"));
            u.setVerified(rs.getBoolean("is_verified"));
            u.setProfileImage(rs.getString("profile_image"));

            list.add(u);
        }

        return list;
    }

    // FIND BY EMAIL
    public Users findByEmail(String email) throws SQLException {

        String sql = "SELECT * FROM users WHERE email=?";

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            Users u = new Users();

            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getString("role"));
            u.setProfileImage(rs.getString("profile_image"));
            u.setTelephone(rs.getString("telephone"));

            return u;
        }

        return null;
    }
    public Users findByPhone(String telephone) throws SQLException {

        String sql = "SELECT * FROM users WHERE telephone = ?";

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, telephone);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Users u = new Users();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setAge(rs.getInt("age"));
            u.setAdresseResidence(rs.getString("adresse_residence"));
            u.setEmail(rs.getString("email"));
            u.setTelephone(rs.getString("telephone"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getString("role"));
            u.setCreatedAt(rs.getTimestamp("created_at"));
            u.setUpdatedAt(rs.getTimestamp("updated_at"));
            u.setVerified(rs.getBoolean("is_verified"));
            u.setProfileImage(rs.getString("profile_image"));
            return u;
        }

        return null;
    }

}
