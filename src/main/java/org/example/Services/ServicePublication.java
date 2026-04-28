package org.example.Services;

import org.example.entities.Publication;
import org.example.repositories.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ServicePublication {

    @Autowired
    private PublicationRepository publicationRepository;

    // CREATE
    public Publication ajouterPublication(Publication p) {
        p.setDateCreation(new Date());
        p.setNombreVues(0);
        p.setNombreLikes(0);
        p.setNombreDislikes(0); // ✅
        return publicationRepository.save(p);
    }

    // READ ALL
    public List<Publication> getAllPublications() {
        return publicationRepository.findAll();
    }

    // READ ONE
    public Publication getPublicationById(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication introuvable : " + id));
    }

    // UPDATE
    public Publication modifierPublication(Long id, Publication nouvelleDonnees) {
        Publication p = getPublicationById(id);
        p.setTitre(nouvelleDonnees.getTitre());
        p.setDescription(nouvelleDonnees.getDescription());
        p.setCategorie(nouvelleDonnees.getCategorie());
        return publicationRepository.save(p);
    }

    // DELETE
    public void supprimerPublication(Long id) {
        publicationRepository.deleteById(id);
    }

    // LIKE ✅
    public Publication likerPublication(Long id) {
        Publication p = getPublicationById(id);
        p.setNombreLikes(p.getNombreLikes() + 1);
        p.setNombreVues(p.getNombreVues() + 1);
        return publicationRepository.save(p);
    }

    // DISLIKE ✅ — version JPA (plus de JDBC)
    public Publication dislikerPublication(Long id) {
        Publication p = getPublicationById(id);
        p.setNombreDislikes(p.getNombreDislikes() + 1);
        return publicationRepository.save(p);
    }

    // ANNULER DISLIKE ✅
    public Publication annulerDislike(Long id) {
        Publication p = getPublicationById(id);
        if (p.getNombreDislikes() > 0) {
            p.setNombreDislikes(p.getNombreDislikes() - 1);
        }
        return publicationRepository.save(p);
    }

    // INCRÉMENTER VUES ✅
    public Publication incrementerVues(Long id) {
        Publication p = getPublicationById(id);
        p.setNombreVues(p.getNombreVues() + 1);
        return publicationRepository.save(p);
    }
}