package org.example.entities;

import java.sql.Timestamp;

public class Users {

    private int id;
    private String nom;
    private String prenom;
    private Integer age;
    private String adresseResidence;
    private String email;
    private String telephone;
    private String password;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isVerified;
    private String profileImage;

    public Users() {}

    public Users(int id, String nom, String prenom, Integer age,
                 String adresseResidence, String email, String telephone,
                 String password, String role, Timestamp createdAt,
                 Timestamp updatedAt, boolean isVerified, String profileImage) {

        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.adresseResidence = adresseResidence;
        this.email = email;
        this.telephone = telephone;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isVerified = isVerified;
        this.profileImage = profileImage;
    }

    // GETTERS / SETTERS

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAdresseResidence() { return adresseResidence; }
    public void setAdresseResidence(String adresseResidence) { this.adresseResidence = adresseResidence; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
