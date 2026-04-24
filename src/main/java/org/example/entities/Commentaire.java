package org.example.entities;

import java.sql.Date;

public class Commentaire {
    private int id;
    private String contenu;
    private Date datecre;
    private String auteur;
    private int nbvue;
    private int likes;
    private int examen_id;

    // Constructeur vide
    public Commentaire() {}

    public Commentaire(int id, String contenu, Date datecre, String auteur, int nbvue, int likes, int examen_id) {
        this.id = id;
        this.contenu = contenu;
        this.datecre = datecre;
        this.auteur = auteur;
        this.nbvue = nbvue;
        this.likes = likes;
        this.examen_id = examen_id;
    }

    // Constructeur complet
    public Commentaire(String contenu, Date datecre, String auteur, int nbvue, int likes, int examen_id) {
        this.contenu = contenu;
        this.datecre = datecre;
        this.auteur = auteur;
        this.nbvue = nbvue;
        this.likes = likes;
        this.examen_id = examen_id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDatecre() { return datecre; }
    public void setDatecre(Date datecre) { this.datecre = datecre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public int getNbvue() { return nbvue; }
    public void setNbvue(int nbvue) { this.nbvue = nbvue; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getExamen_id() { return examen_id; }
    public void setExamen_id(int examen_id) { this.examen_id = examen_id; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", datecre=" + datecre +
                ", auteur='" + auteur + '\'' +
                ", nbvue=" + nbvue +
                ", likes=" + likes +
                ", examen_id=" + examen_id +
                '}';
    }
}