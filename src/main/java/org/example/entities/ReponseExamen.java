package org.example.entities;

import java.sql.Timestamp;

public class ReponseExamen {
    private int id;
    private int examenId;
    private int userId;
    private String contenu;
    private Timestamp dateSoumission;
    private Double note;
    private String commentaireCorrection;
    private Timestamp corrigeLe;

    // Constructeurs
    public ReponseExamen() {}

    public ReponseExamen(int examenId, int userId, String contenu) {
        this.examenId = examenId;
        this.userId = userId;
        this.contenu = contenu;
        this.dateSoumission = new Timestamp(System.currentTimeMillis());
    }

    public ReponseExamen(int id, int examenId, int userId, String contenu,
                         Timestamp dateSoumission, Double note,
                         String commentaireCorrection, Timestamp corrigeLe) {
        this.id = id;
        this.examenId = examenId;
        this.userId = userId;
        this.contenu = contenu;
        this.dateSoumission = dateSoumission;
        this.note = note;
        this.commentaireCorrection = commentaireCorrection;
        this.corrigeLe = corrigeLe;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamenId() {
        return examenId;
    }

    public void setExamenId(int examenId) {
        this.examenId = examenId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Timestamp getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(Timestamp dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public String getCommentaireCorrection() {
        return commentaireCorrection;
    }

    public void setCommentaireCorrection(String commentaireCorrection) {
        this.commentaireCorrection = commentaireCorrection;
    }

    public Timestamp getCorrigeLe() {
        return corrigeLe;
    }

    public void setCorrigeLe(Timestamp corrigeLe) {
        this.corrigeLe = corrigeLe;
    }

    @Override
    public String toString() {
        return "ReponseExamen{" +
                "id=" + id +
                ", examenId=" + examenId +
                ", userId=" + userId +
                ", dateSoumission=" + dateSoumission +
                ", note=" + note +
                ", corrigeLe=" + corrigeLe +
                '}';
    }
}
