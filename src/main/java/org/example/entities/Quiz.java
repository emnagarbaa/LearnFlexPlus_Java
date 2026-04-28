package org.example.entities;
import java.util.ArrayList;
import java.util.List;

public class Quiz {

    private int id;
    private String titre;
    private String question;
    private int duree;
    private String etat;
    private String description;

    private List<Reponse> reponses = new ArrayList<>();

    public Quiz() {}

    public Quiz(String titre, String question, int duree, String etat, String description) {
        this.titre = titre;
        this.question = question;
        this.duree = duree;
        this.etat = etat;
        this.description = description;
        this.reponses = new ArrayList<>();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Reponse> getReponses() {
        return reponses;
    }

    public void setReponses(List<Reponse> reponses) {
        this.reponses = reponses;
    }


    @Override
    public String toString() {
        return String.format(
                "Quiz{id=%d, titre='%s', question='%s', duree=%d min, etat='%s', description='%s', reponses=%d}",
                id, titre, question, duree, etat,
                description != null ? description : "N/A",
                reponses.size()
        );
    }
}
