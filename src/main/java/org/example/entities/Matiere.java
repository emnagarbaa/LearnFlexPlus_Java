package org.example.entities;

import java.sql.Timestamp;

public class Matiere {
    private int id;
    private String nomMatiere;
    private String description;
    private String section;
    private String codeMatiere;
    private Timestamp dateCreation;
    private String niveau;
    private String image;

    // Constructeur vide
    public Matiere() {
        this.dateCreation = new Timestamp(System.currentTimeMillis());
    }

    // Constructeur sans id
    public Matiere(String nomMatiere, String description, String section, String codeMatiere, String niveau, String image) {
        this.nomMatiere = nomMatiere;
        this.description = description;
        this.section = section;
        this.codeMatiere = codeMatiere;
        this.dateCreation = new Timestamp(System.currentTimeMillis());
        this.niveau = niveau;
        this.image = image;
    }

    // Constructeur complet
    public Matiere(int id, String nomMatiere, String description, String section, String codeMatiere, Timestamp dateCreation, String niveau, String image) {
        this.id = id;
        this.nomMatiere = nomMatiere;
        this.description = description;
        this.section = section;
        this.codeMatiere = codeMatiere;
        this.dateCreation = dateCreation;
        this.niveau = niveau;
        this.image = image;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomMatiere() { return nomMatiere; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getCodeMatiere() { return codeMatiere; }
    public void setCodeMatiere(String codeMatiere) { this.codeMatiere = codeMatiere; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return nomMatiere != null ? nomMatiere : "";
    }
}