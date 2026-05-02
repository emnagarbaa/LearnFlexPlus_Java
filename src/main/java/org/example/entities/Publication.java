package org.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "publication")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // ✅ aligné avec MySQL
    private Long idPublication;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit avoir entre 3 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 500, message = "La description doit avoir entre 10 et 500 caractères")
    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    @Column(length = 50)
    private String categorie;

    @Min(value = 0, message = "Le nombre de vues ne peut pas être négatif")
    @Column(name = "nombre_vues")
    private int nombreVues = 0;

    @Min(value = 0, message = "Le nombre de likes ne peut pas être négatif")
    @Column(name = "nombre_likes")
    private int nombreLikes = 0;

    @Min(value = 0, message = "Le nombre de dislikes ne peut pas être négatif")
    @Column(name = "nombre_dislikes")
    private int nombreDislikes = 0;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL)
    private List<Communication> communications;

    // Getters & Setters
    public Long getIdPublication() { return idPublication; }
    public void setIdPublication(Long idPublication) { this.idPublication = idPublication; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getNombreVues() { return nombreVues; }
    public void setNombreVues(int nombreVues) { this.nombreVues = nombreVues; }

    public int getNombreLikes() { return nombreLikes; }
    public void setNombreLikes(int nombreLikes) { this.nombreLikes = nombreLikes; }

    public int getNombreDislikes() { return nombreDislikes; }
    public void setNombreDislikes(int nombreDislikes) { this.nombreDislikes = nombreDislikes; }

    public List<Communication> getCommunications() { return communications; }
    public void setCommunications(List<Communication> communications) { this.communications = communications; }
}