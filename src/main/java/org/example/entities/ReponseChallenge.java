package org.example.entities;

import java.sql.Timestamp;

public class ReponseChallenge {

    private int      id;
    private int      challengeId;
    private int      userId;
    private String   reponseTexte;
    private Timestamp dateSoumission;
    private String   statut;        // "En attente" | "Validé" | "Rejeté"
    private Float    note;          // null si pas encore noté
    private String   commentaire;
    private Timestamp dateCorrection;

    public ReponseChallenge() {}

    public ReponseChallenge(int challengeId, int userId, String reponseTexte) {
        this.challengeId  = challengeId;
        this.userId       = userId;
        this.reponseTexte = reponseTexte;
        this.statut       = "En attente";
    }

    // ── Getters / Setters ─────────────────────────────────
    public int       getId()               { return id; }
    public void      setId(int id)         { this.id = id; }

    public int       getChallengeId()                     { return challengeId; }
    public void      setChallengeId(int challengeId)      { this.challengeId = challengeId; }

    public int       getUserId()                          { return userId; }
    public void      setUserId(int userId)                { this.userId = userId; }

    public String    getReponseTexte()                    { return reponseTexte; }
    public void      setReponseTexte(String reponseTexte) { this.reponseTexte = reponseTexte; }

    public Timestamp getDateSoumission()                        { return dateSoumission; }
    public void      setDateSoumission(Timestamp dateSoumission){ this.dateSoumission = dateSoumission; }

    public String    getStatut()                          { return statut; }
    public void      setStatut(String statut)             { this.statut = statut; }

    public Float     getNote()                            { return note; }
    public void      setNote(Float note)                  { this.note = note; }

    public String    getCommentaire()                     { return commentaire; }
    public void      setCommentaire(String commentaire)   { this.commentaire = commentaire; }

    public Timestamp getDateCorrection()                        { return dateCorrection; }
    public void      setDateCorrection(Timestamp dateCorrection){ this.dateCorrection = dateCorrection; }

    @Override
    public String toString() {
        return "ReponseChallenge{id=" + id + ", challengeId=" + challengeId +
                ", userId=" + userId + ", statut='" + statut + "'}";
    }
}
