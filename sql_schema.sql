-- ============================================
-- LearnFlexPlus - Tables pour Matière et Cours
-- ============================================

-- Table Matière (Chapitre/Subject)
CREATE TABLE IF NOT EXISTS matiere (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_matiere VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    section VARCHAR(255) NOT NULL,
    code_matiere VARCHAR(50) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    niveau VARCHAR(255) NOT NULL,
    image VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table Cours (Course)
CREATE TABLE IF NOT EXISTS cours (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    section VARCHAR(255) NOT NULL,
    duree_totale VARCHAR(255) NOT NULL,
    langue VARCHAR(50) NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    pdf_file VARCHAR(255) DEFAULT NULL,
    matiere_id INT NOT NULL,
    prix DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_cours_matiere FOREIGN KEY (matiere_id) REFERENCES matiere(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Données de test (optionnel)
-- ============================================
INSERT INTO matiere (nom_matiere, description, section, code_matiere, niveau, image) VALUES
('Mathématiques', 'Cours complet de mathématiques couvrant l''algèbre, la géométrie et l''analyse.', 'Sciences', 'MATH101', 'Bac 2ème année', NULL),
('Physique', 'Mécanique, thermodynamique et électromagnétisme pour les élèves du bac.', 'Sciences', 'PHYS101', 'Bac 2ème année', NULL),
('Informatique', 'Algorithmique, programmation et bases de données.', 'Technique', 'INFO101', 'Bac 3ème année', NULL),
('Français', 'Littérature, grammaire et expression écrite.', 'Lettres', 'FR101', 'Bac 1ère année', NULL);

INSERT INTO cours (titre, description, section, duree_totale, langue, matiere_id, prix) VALUES
('Algèbre linéaire', 'Introduction aux espaces vectoriels, matrices et déterminants.', 'Chapitre 1', '3h00', 'Français', 1, 19.99),
('Géométrie dans l''espace', 'Droites, plans et solides dans l''espace euclidien.', 'Chapitre 2', '2h30', 'Français', 1, 14.99),
('Mécanique du point', 'Cinématique et dynamique du point matériel.', 'Chapitre 1', '4h00', 'Français', 2, 24.99),
('Algorithmes de tri', 'Tri par insertion, sélection, fusion et tri rapide.', 'Chapitre 3', '2h00', 'Français', 3, 9.99),
('Bases de données SQL', 'Modélisation, requêtes SQL et normalisation.', 'Chapitre 5', '3h30', 'Français', 3, 29.99);
