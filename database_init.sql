-- ═══════════════════════════════════════════════════════════════
-- Database Initialization Script for LearnFlexPlus
-- ═══════════════════════════════════════════════════════════════

-- Create Database
CREATE DATABASE IF NOT EXISTS learnflexplus;
USE learnflexplus;

-- ─────────────────────────────────────────────────────────────
-- Table: organisme
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS organisme (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    description LONGTEXT,
    siteWeb VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(20),
    ville VARCHAR(100),
    actif BOOLEAN DEFAULT TRUE,
    fraisMin DOUBLE DEFAULT 0,
    langue VARCHAR(10),
    opportunitesStage BOOLEAN DEFAULT FALSE,
    opportunitesEmploi BOOLEAN DEFAULT FALSE,
    photo LONGBLOB
);

-- ─────────────────────────────────────────────────────────────
-- Table: evenement
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS evenement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    description LONGTEXT,
    dateDebut DATETIME,
    dateFin DATETIME,
    lieu VARCHAR(255),
    mode VARCHAR(50),
    capaciteMax INT DEFAULT 0,
    publicCible VARCHAR(255),
    organisme_id INT,
    contactEmail VARCHAR(255),
    contactTelephone VARCHAR(20),
    lienInscription VARCHAR(255),
    FOREIGN KEY (organisme_id) REFERENCES organisme(id) ON DELETE SET NULL
);

-- ─────────────────────────────────────────────────────────────
-- Sample Data: organisme
-- ─────────────────────────────────────────────────────────────
INSERT INTO organisme (nom, type, description, siteWeb, email, telephone, ville, actif, fraisMin, langue, opportunitesStage, opportunitesEmploi) VALUES
('ISET Rades', 'École', 'Institut Supérieur des Études Technologiques de Radès', 'https://iset.rn.tn', 'contact@iset.rn.tn', '+216 71 123 456', 'Rades', TRUE, 0, 'fr-FR', TRUE, TRUE),
('Université de Tunis', 'Université', 'Université de Tunis El Manar', 'https://utunis.rn.tn', 'contact@ut.tn', '+216 71 234 567', 'Tunis', TRUE, 0, 'fr-FR', TRUE, TRUE),
('ARIANETECH', 'Entreprise', 'Agence de conseil et de développement informatique', 'https://arianetech.tn', 'info@arianetech.tn', '+216 71 345 678', 'Tunis', TRUE, 25.000, 'fr-FR', TRUE, TRUE);

-- ─────────────────────────────────────────────────────────────
-- Sample Data: evenement
-- ─────────────────────────────────────────────────────────────
INSERT INTO evenement (titre, description, dateDebut, dateFin, lieu, mode, capaciteMax, publicCible, organisme_id, contactEmail, contactTelephone, lienInscription) VALUES
('Semaine du Développement', 'Une semaine dédiée à l''apprentissage des technologies web modernes', '2026-02-26 07:00:00', '2026-02-27 17:00:00', 'Tunis', 'EN LIGNE', 450, 'Étudiants', 1, 'contact@iset.rn.tn', '+216 71 123 456', 'https://iset.rn.tn/inscription'),
('Conférence Cloud Computing', 'Explorez les dernières tendances du Cloud Computing avec les experts', '2026-03-15 14:00:00', '2026-03-15 16:00:00', 'Ariana', 'PRÉSENTIEL', 200, 'Professionnels', 3, 'info@arianetech.tn', '+216 71 345 678', 'https://arianetech.tn/cloud'),
('Atelier Python Avancé', 'Maîtrisez les concepts avancés de Python', '2026-03-20 09:00:00', '2026-03-21 17:00:00', 'Tunis', 'HYBRIDE', 100, 'Développeurs', 1, 'contact@iset.rn.tn', '+216 71 123 456', 'https://iset.rn.tn/python'),
('Forum de l''Emploi Tech', 'Rencontrez les plus grandes entreprises tech de Tunisie', '2026-04-10 08:00:00', '2026-04-10 18:00:00', 'Tunis', 'PRÉSENTIEL', 500, 'Étudiants & Jeunes Diplômés', 2, 'contact@ut.tn', '+216 71 234 567', 'https://utunis.rn.tn/salon-emploi'),
('Certification Google Cloud', 'Préparez-vous pour la certification Google Cloud Professional', '2026-04-01 10:00:00', '2026-04-30 15:00:00', 'En ligne', 'EN LIGNE', 50, 'IT Professionals', 3, 'training@arianetech.tn', '+216 71 345 678', 'https://arianetech.tn/gcp-cert');

-- ─────────────────────────────────────────────────────────────
-- Verify Data
-- ─────────────────────────────────────────────────────────────
SELECT * FROM organisme;
SELECT COUNT(*) as 'Organismes' FROM organisme;

SELECT e.id, e.titre, e.dateDebut, e.lieu, e.capaciteMax, o.nom as organisme
FROM evenement e
LEFT JOIN organisme o ON e.organisme_id = o.id;
SELECT COUNT(*) as 'Événements' FROM evenement;
