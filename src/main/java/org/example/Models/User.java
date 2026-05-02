package org.example.Models;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private int age;
    private String adresse_residence;
    private String email;
    private String telephone;
    private String password;
    private String role;
    private String image;

    public User(int id, String username, String email, String password, String role, String image) {
        this.id = id;
        this.nom = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.image = image;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getAdresse_residence() { return adresse_residence; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getImage() { return image; }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(int age) { this.age = age; }
    public void setAdresse_residence(String adresse_residence) { this.adresse_residence = adresse_residence; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setImage(String image) { this.image = image; }
}
