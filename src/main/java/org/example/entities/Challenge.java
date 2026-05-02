package org.example.entities;

import java.sql.Date;
import java.sql.Timestamp;

public class Challenge {
    private int id;
    private String titrec;
    private String descriptionc;
    private double objectifscore;
    private double progressionactuelle;
    private String niveaudifficulte;
    private String niveauatteint;
    private String typerecomponse;
    private String contenurecompense;
    private String etat;
    private Date dated;
    private Date datef;
    private Date datelimite;
    private boolean alerte;
    private String question;
    private String images;
    private int dernier_score;
    private String dernier_niveau;
    private String reponses;
    private String interacty_hash;
    private Timestamp created_at;
    private Timestamp updated_at;
    private int examen_id;

    // Constructeur vide
    public Challenge() {}

    public Challenge(int id, String titrec, String descriptionc, double objectifscore, double progressionactuelle, String niveaudifficulte, String niveauatteint, String typerecomponse, String contenurecompense, String etat, Date dated, Date datef, Date datelimite, boolean alerte, String question, String images, int dernier_score, String dernier_niveau, String reponses, String interacty_hash, Timestamp created_at, Timestamp updated_at, int examen_id) {
        this.id = id;
        this.titrec = titrec;
        this.descriptionc = descriptionc;
        this.objectifscore = objectifscore;
        this.progressionactuelle = progressionactuelle;
        this.niveaudifficulte = niveaudifficulte;
        this.niveauatteint = niveauatteint;
        this.typerecomponse = typerecomponse;
        this.contenurecompense = contenurecompense;
        this.etat = etat;
        this.dated = dated;
        this.datef = datef;
        this.datelimite = datelimite;
        this.alerte = alerte;
        this.question = question;
        this.images = images;
        this.dernier_score = dernier_score;
        this.dernier_niveau = dernier_niveau;
        this.reponses = reponses;
        this.interacty_hash = interacty_hash;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.examen_id = examen_id;
    }

    public Challenge(String titrec, String descriptionc, double objectifscore, double progressionactuelle, String niveaudifficulte, String niveauatteint, String typerecomponse, String contenurecompense, String etat, Date dated, Date datef, Date datelimite, boolean alerte, String question, String images, int dernier_score, String dernier_niveau, String reponses, String interacty_hash, Timestamp created_at, Timestamp updated_at, int examen_id) {
        this.titrec = titrec;
        this.descriptionc = descriptionc;
        this.objectifscore = objectifscore;
        this.progressionactuelle = progressionactuelle;
        this.niveaudifficulte = niveaudifficulte;
        this.niveauatteint = niveauatteint;
        this.typerecomponse = typerecomponse;
        this.contenurecompense = contenurecompense;
        this.etat = etat;
        this.dated = dated;
        this.datef = datef;
        this.datelimite = datelimite;
        this.alerte = alerte;
        this.question = question;
        this.images = images;
        this.dernier_score = dernier_score;
        this.dernier_niveau = dernier_niveau;
        this.reponses = reponses;
        this.interacty_hash = interacty_hash;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.examen_id = examen_id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitrec() { return titrec; }
    public void setTitrec(String titrec) { this.titrec = titrec; }

    public String getDescriptionc() { return descriptionc; }
    public void setDescriptionc(String descriptionc) { this.descriptionc = descriptionc; }

    public double getObjectifscore() { return objectifscore; }
    public void setObjectifscore(double objectifscore) { this.objectifscore = objectifscore; }

    public double getProgressionactuelle() { return progressionactuelle; }
    public void setProgressionactuelle(double progressionactuelle) { this.progressionactuelle = progressionactuelle; }

    public String getNiveaudifficulte() { return niveaudifficulte; }
    public void setNiveaudifficulte(String niveaudifficulte) { this.niveaudifficulte = niveaudifficulte; }

    public String getNiveauatteint() { return niveauatteint; }
    public void setNiveauatteint(String niveauatteint) { this.niveauatteint = niveauatteint; }

    public String getTyperecomponse() { return typerecomponse; }
    public void setTyperecomponse(String typerecomponse) { this.typerecomponse = typerecomponse; }

    public String getContenurecompense() { return contenurecompense; }
    public void setContenurecompense(String contenurecompense) { this.contenurecompense = contenurecompense; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public Date getDated() { return dated; }
    public void setDated(Date dated) { this.dated = dated; }

    public Date getDatef() { return datef; }
    public void setDatef(Date datef) { this.datef = datef; }

    public Date getDatelimite() { return datelimite; }
    public void setDatelimite(Date datelimite) { this.datelimite = datelimite; }

    public boolean isAlerte() { return alerte; }
    public void setAlerte(boolean alerte) { this.alerte = alerte; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public int getDernier_score() { return dernier_score; }
    public void setDernier_score(int dernier_score) { this.dernier_score = dernier_score; }

    public String getDernier_niveau() { return dernier_niveau; }
    public void setDernier_niveau(String dernier_niveau) { this.dernier_niveau = dernier_niveau; }

    public String getReponses() { return reponses; }
    public void setReponses(String reponses) { this.reponses = reponses; }

    public String getInteracty_hash() { return interacty_hash; }
    public void setInteracty_hash(String interacty_hash) { this.interacty_hash = interacty_hash; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public Timestamp getUpdated_at() { return updated_at; }
    public void setUpdated_at(Timestamp updated_at) { this.updated_at = updated_at; }

    public int getExamen_id() { return examen_id; }
    public void setExamen_id(int examen_id) { this.examen_id = examen_id; }

    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", titrec='" + titrec + '\'' +
                ", descriptionc='" + descriptionc + '\'' +
                ", objectifscore=" + objectifscore +
                ", progressionactuelle=" + progressionactuelle +
                ", niveaudifficulte='" + niveaudifficulte + '\'' +
                ", etat='" + etat + '\'' +
                ", examen_id=" + examen_id +
                '}';
    }
}
