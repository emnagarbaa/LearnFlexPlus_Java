package org.example.entities;

public class Organisme {
    private int id;
    private String nom;
    private String type;
    private String description;
    private String siteWeb;
    private String email;
    private String telephone;
    private String ville;
    private boolean actif;
    private double fraisMin;
    private String langue;
    private boolean opportunitesStage;
    private boolean opportunitesEmploi;
    private String photo;

    public Organisme() {}

    public Organisme(int id, String nom, String type, String description,
                     String siteWeb, String email, String telephone, String ville,
                     boolean actif, double fraisMin, String langue,
                     boolean opportunitesStage, boolean opportunitesEmploi, String photo) {
        this.id = id; this.nom = nom; this.type = type;
        this.description = description; this.siteWeb = siteWeb;
        this.email = email; this.telephone = telephone; this.ville = ville;
        this.actif = actif; this.fraisMin = fraisMin; this.langue = langue;
        this.opportunitesStage = opportunitesStage;
        this.opportunitesEmploi = opportunitesEmploi; this.photo = photo;
    }

    public int getId()                         { return id; }
    public void setId(int id)                  { this.id = id; }
    public String getNom()                     { return nom; }
    public void setNom(String nom)             { this.nom = nom; }
    public String getType()                    { return type; }
    public void setType(String type)           { this.type = type; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public String getSiteWeb()                 { return siteWeb; }
    public void setSiteWeb(String s)           { this.siteWeb = s; }
    public String getEmail()                   { return email; }
    public void setEmail(String e)             { this.email = e; }
    public String getTelephone()               { return telephone; }
    public void setTelephone(String t)         { this.telephone = t; }
    public String getVille()                   { return ville; }
    public void setVille(String v)             { this.ville = v; }
    public boolean isActif()                   { return actif; }
    public void setActif(boolean a)            { this.actif = a; }
    public double getFraisMin()                { return fraisMin; }
    public void setFraisMin(double f)          { this.fraisMin = f; }
    public String getLangue()                  { return langue; }
    public void setLangue(String l)            { this.langue = l; }
    public boolean isOpportunitesStage()       { return opportunitesStage; }
    public void setOpportunitesStage(boolean b){ this.opportunitesStage = b; }
    public boolean isOpportunitesEmploi()      { return opportunitesEmploi; }
    public void setOpportunitesEmploi(boolean b){ this.opportunitesEmploi = b; }
    public String getPhoto()                   { return photo; }
    public void setPhoto(String p)             { this.photo = p; }

}
