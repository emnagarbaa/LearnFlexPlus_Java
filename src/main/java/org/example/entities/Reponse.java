package org.example.entities;

public class Reponse {


    private int id;
    private String texte;
    private boolean estCorrecte;
    private Quiz quiz; // relation ManyToOne

    // ─── Constructeur par défaut ───────────────────────────────────────────────
    public Reponse() {
    }
    // ─── Constructeur complet ──────────────────────────────────────────────────
    public Reponse(String texte, boolean estCorrecte, Quiz quiz) {
        this.texte       = texte;
        this.estCorrecte = estCorrecte;
        this.quiz        = quiz;
    }
    // ─── Getters & Setters ─────────────────────────────────────────────────────
    public int getId() { return id; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public boolean isEstCorrecte() { return estCorrecte; }
    public void setEstCorrecte(boolean estCorrecte) { this.estCorrecte = estCorrecte; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    // ─── Affichage ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
                "  Reponse{id=%d, texte='%s', correcte=%s}",
                id, texte, estCorrecte ? "✔ OUI" : "✘ NON"
        );
    }

    public void setId(int id) {
        this.id = id;
    }
}