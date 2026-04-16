package org.example.entities;

import java.time.LocalDateTime;

public class Evenement {

    private int id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private String mode;
    private int capaciteMax;
    private String publicCible;
    private Organisme organisme;
    private String contactEmail;
    private String contactTelephone;
    private String lienInscription;

    public Evenement() {}

    public Evenement(int id, String titre, String description,
                     LocalDateTime dateDebut, LocalDateTime dateFin,
                     String lieu, String mode, int capaciteMax,
                     String publicCible, Organisme organisme,
                     String contactEmail, String contactTelephone,
                     String lienInscription) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.mode = mode;
        this.capaciteMax = capaciteMax;
        this.publicCible = publicCible;
        this.organisme = organisme;
        this.contactEmail = contactEmail;
        this.contactTelephone = contactTelephone;
        this.lienInscription = lienInscription;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getPublicCible() { return publicCible; }
    public void setPublicCible(String publicCible) { this.publicCible = publicCible; }

    public Organisme getOrganisme() { return organisme; }
    public void setOrganisme(Organisme organisme) { this.organisme = organisme; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactTelephone() { return contactTelephone; }
    public void setContactTelephone(String contactTelephone) { this.contactTelephone = contactTelephone; }

    public String getLienInscription() { return lienInscription; }
    public void setLienInscription(String lienInscription) { this.lienInscription = lienInscription; }
}