package org.example.entities;

import java.sql.Timestamp;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private Timestamp dateCreation;
    private String section;
    private String dureeTotale;
    private String langue;
    private String image;
    private String pdfFile;
    private int matiereId;
    private double prix;

    // Transient - for display purposes
    private Matiere matiere;

    // Constructeur vide
    public Cours() {
        this.dateCreation = new Timestamp(System.currentTimeMillis());
    }

    // Constructeur sans id
    public Cours(String titre, String description, String section, String dureeTotale, String langue, String image, String pdfFile, int matiereId, double prix) {
        this.titre = titre;
        this.description = description;
        this.dateCreation = new Timestamp(System.currentTimeMillis());
        this.section = section;
        this.dureeTotale = dureeTotale;
        this.langue = langue;
        this.image = image;
        this.pdfFile = pdfFile;
        this.matiereId = matiereId;
        this.prix = prix;
    }

    // Constructeur complet
    public Cours(int id, String titre, String description, Timestamp dateCreation, String section, String dureeTotale, String langue, String image, String pdfFile, int matiereId, double prix) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.section = section;
        this.dureeTotale = dureeTotale;
        this.langue = langue;
        this.image = image;
        this.pdfFile = pdfFile;
        this.matiereId = matiereId;
        this.prix = prix;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDureeTotale() { return dureeTotale; }
    public void setDureeTotale(String dureeTotale) { this.dureeTotale = dureeTotale; }

    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getPdfFile() { return pdfFile; }
    public void setPdfFile(String pdfFile) { this.pdfFile = pdfFile; }

    public int getMatiereId() { return matiereId; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", matiereId=" + matiereId +
                ", prix=" + prix +
                '}';
    }
}
