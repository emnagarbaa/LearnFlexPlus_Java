package org.example.entities;

import java.sql.Date;

public class Examen {
    private int id;
    private String titre;
    private String description;
    private String matiere;
    private String niveauexamen;
    private Date datedebut;
    private Date datefin;
    private int duree;
    private int nbquestion;
    private double scoretotal;
    private double coefficient;
    private String typeexamen;
    private String etat;
    private String pdf;
    private String questions;

    // Constructeur vide
    public Examen() {}

    public Examen(int id, String titre, String description, String matiere, String niveauexamen, Date datedebut, Date datefin, int duree, int nbquestion, double scoretotal, double coefficient, String typeexamen, String etat, String pdf, String questions) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.matiere = matiere;
        this.niveauexamen = niveauexamen;
        this.datedebut = datedebut;
        this.datefin = datefin;
        this.duree = duree;
        this.nbquestion = nbquestion;
        this.scoretotal = scoretotal;
        this.coefficient = coefficient;
        this.typeexamen = typeexamen;
        this.etat = etat;
        this.pdf = pdf;
        this.questions = questions;
    }

    // Constructeur complet (sans id, car AUTO_INCREMENT)
    public Examen(String titre, String description, String matiere, String niveauexamen,
                  Date datedebut, Date datefin, int duree, int nbquestion,
                  double scoretotal, double coefficient, String typeexamen,
                  String etat, String pdf, String questions) {
        this.titre = titre;
        this.description = description;
        this.matiere = matiere;
        this.niveauexamen = niveauexamen;
        this.datedebut = datedebut;
        this.datefin = datefin;
        this.duree = duree;
        this.nbquestion = nbquestion;
        this.scoretotal = scoretotal;
        this.coefficient = coefficient;
        this.typeexamen = typeexamen;
        this.etat = etat;
        this.pdf = pdf;
        this.questions = questions;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public String getNiveauexamen() { return niveauexamen; }
    public void setNiveauexamen(String niveauexamen) { this.niveauexamen = niveauexamen; }

    public Date getDatedebut() { return datedebut; }
    public void setDatedebut(Date datedebut) { this.datedebut = datedebut; }

    public Date getDatefin() { return datefin; }
    public void setDatefin(Date datefin) { this.datefin = datefin; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public int getNbquestion() { return nbquestion; }
    public void setNbquestion(int nbquestion) { this.nbquestion = nbquestion; }

    public double getScoretotal() { return scoretotal; }
    public void setScoretotal(double scoretotal) { this.scoretotal = scoretotal; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public String getTypeexamen() { return typeexamen; }
    public void setTypeexamen(String typeexamen) { this.typeexamen = typeexamen; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getPdf() { return pdf; }
    public void setPdf(String pdf) { this.pdf = pdf; }

    public String getQuestions() { return questions; }
    public void setQuestions(String questions) { this.questions = questions; }

    @Override
    public String toString() {
        return "Examen{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", matiere='" + matiere + '\'' +
                ", niveauexamen='" + niveauexamen + '\'' +
                ", datedebut=" + datedebut +
                ", datefin=" + datefin +
                ", duree=" + duree +
                ", nbquestion=" + nbquestion +
                ", scoretotal=" + scoretotal +
                ", coefficient=" + coefficient +
                ", typeexamen='" + typeexamen + '\'' +
                ", etat='" + etat + '\'' +
                '}';
    }
}