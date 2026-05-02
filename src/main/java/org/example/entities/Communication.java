package org.example.entities;

import jakarta.persistence.*;
import java.util.Date;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "communication")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_communication")
    private Long idCommunication;

    // ✅ VALIDATION : seulement "live" ou "record"
    @NotBlank(message = "Le type est obligatoire")
    @Pattern(regexp = "live|record", message = "Le type doit être 'live' ou 'record'")
    @Column(nullable = false)
    private String type;

    // ✅ VALIDATION : doit être une URL valide
    @NotBlank(message = "Le lien est obligatoire")
    @Pattern(regexp = "^(https?://).*", message = "Le lien doit être une URL valide (http ou https)")
    @Column(nullable = false)
    private String lien;

    // ✅ VALIDATION : date obligatoire et doit être dans le futur
    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date doit être dans le futur")
    @Column(name = "date_heure")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateHeure;

    // ✅ VALIDATION : durée minimum 1 minute, maximum 480 minutes (8h)
    @Min(value = 1, message = "La durée doit être au moins 1 minute")
    @Max(value = 480, message = "La durée ne peut pas dépasser 480 minutes")
    @Column(nullable = false)
    private int duree;

    // ✅ VALIDATION : id user obligatoire
    @NotNull(message = "L'utilisateur est obligatoire")
    @Column(name = "id_user", nullable = false)
    private Long idUser;

    // ✅ VALIDATION : état obligatoire
    @NotBlank(message = "L'état est obligatoire")
    @Pattern(regexp = "actif|terminé|annulé", message = "L'état doit être 'actif', 'terminé' ou 'annulé'")
    @Column(nullable = false, length = 20)
    private String etat;

    // ✅ VALIDATION : description optionnelle mais limitée
    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    @Column(name = "description_detaillee", length = 1000)
    private String descriptionDetaillee;

    // ✅ VALIDATION : publication obligatoire
    @NotNull(message = "La publication associée est obligatoire")
    @ManyToOne
    @JoinColumn(name = "id_publication", nullable = false)
    private Publication publication;

    // Getters & Setters — inchangés
    public Long getIdCommunication() { return idCommunication; }
    public void setIdCommunication(Long idCommunication) { this.idCommunication = idCommunication; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public Date getDateHeure() { return dateHeure; }
    public void setDateHeure(Date dateHeure) { this.dateHeure = dateHeure; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getDescriptionDetaillee() { return descriptionDetaillee; }
    public void setDescriptionDetaillee(String descriptionDetaillee) { this.descriptionDetaillee = descriptionDetaillee; }

    public Publication getPublication() { return publication; }
    public void setPublication(Publication publication) { this.publication = publication; }
}