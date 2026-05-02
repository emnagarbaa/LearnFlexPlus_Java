package org.example.Services;

import org.example.entities.Communication;
import org.example.repositories.CommunicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCommunication {

    @Autowired
    private CommunicationRepository communicationRepository;

    // CREATE
    public Communication ajouterCommunication(Communication c) {
        return communicationRepository.save(c);
    }

    // READ ALL
    public List<Communication> getAllCommunications() {
        return communicationRepository.findAll();
    }

    // READ ONE
    public Communication getCommunicationById(Long id) {
        return communicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Communication introuvable : " + id));
    }

    // UPDATE
    public Communication modifierCommunication(Long id, Communication nouvelleDonnees) {
        Communication c = getCommunicationById(id);
        c.setType(nouvelleDonnees.getType());
        c.setLien(nouvelleDonnees.getLien());
        c.setDateHeure(nouvelleDonnees.getDateHeure());
        c.setDuree(nouvelleDonnees.getDuree());
        c.setEtat(nouvelleDonnees.getEtat());
        c.setDescriptionDetaillee(nouvelleDonnees.getDescriptionDetaillee());
        return communicationRepository.save(c);
    }

    // DELETE
    public void supprimerCommunication(Long id) {
        communicationRepository.deleteById(id);
    }
}