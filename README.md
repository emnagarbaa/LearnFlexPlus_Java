# 🎓 LearnFlex Plus

**LearnFlex Plus** est une plateforme e-learning intelligente conçue pour accompagner les élèves dans leur apprentissage, leur évaluation, leur orientation académique et leur interaction avec les enseignants.

L’application centralise plusieurs fonctionnalités pédagogiques dans un seul espace moderne et intuitif :  
📚 gestion des cours,  
📝 examens et quiz,  
🎯 orientation académique,  
💬 forum éducatif,  
🎥 communication en direct ou enregistrée avec les professeurs.

---

## 📌 Objectif du projet

L’objectif principal de **LearnFlex Plus** est de proposer une solution numérique flexible permettant aux élèves de :

- suivre leurs cours en ligne,
- accéder à des leçons structurées,
- passer des quiz et examens,
- consulter leurs résultats,
- bénéficier d’une orientation personnalisée,
- échanger avec leurs enseignants,
- participer à une communauté éducative active.

---

## 🚀 Fonctionnalités principales

### 👤 1. Gestion des utilisateurs
- Inscription / connexion
- Gestion des rôles :
  - Étudiant
  - Enseignant
  - Administrateur
- Gestion du profil utilisateur

### 📚 2. Gestion des cours
- Création de cours
- Organisation des leçons
- Ajout de contenu pédagogique
- Classement par niveau / catégorie

### 📝 3. Gestion des examens
- Création d’examens
- Définition de la durée et de la date
- Évaluation des performances
- Système de review / feedback

### ❓ 4. Gestion des quiz
- Création de quiz interactifs
- Ajout de questions et réponses
- Correction automatique
- Attribution de score

### 🎯 5. Gestion de l’orientation
- Attribution de scores d’orientation
- Organisation d’événements éducatifs
- Aide à la prise de décision académique

### 💬 6. Gestion du forum
- Publications éducatives
- Partage d’informations
- Interaction entre utilisateurs

### 🎥 7. Communication enseignant - élève
- Sessions **live**
- Vidéos **record**
- Accès à des contenus d’accompagnement
- Suivi pédagogique plus humain et interactif

## 🧱 Architecture fonctionnelle

Le projet est organisé en plusieurs modules :

- **Gestion Utilisateur**
- **Gestion Cours**
- **Gestion Examen**
- **Gestion Quiz**
- **Gestion Orientation**
- **Gestion Forum**
- **Gestion Communication**

Cette séparation facilite :
- la maintenance,
- l’évolutivité,
- l’organisation du code,
- la répartition du travail en équipe.


## 🗂️ Modèle conceptuel (Entités principales)

### 👤 User
Représente les utilisateurs de la plateforme :
- étudiant
- enseignant
- administrateur

### 📚 Cours / Leçon
Permet de structurer le contenu pédagogique.

### 📝 Examen / Review
Permet d’évaluer les élèves et de leur fournir un retour.

### ❓ Quiz / Question / Réponse
Permet l’apprentissage interactif et l’auto-évaluation.

### 🎯 Score / Événement
Permet de soutenir l’orientation scolaire et académique.

### 💬 Publication
Permet aux utilisateurs de partager du contenu dans le forum.

### 🎥 Communication
Permet d’organiser :
- des lives,
- des vidéos enregistrées,
- des échanges pédagogiques.

### Front-end
-Symfony
- JavaFX

### Back-end
- PHP / Symfony/Java 
- MySQL

### Outils de développement
- Git & GitHub
- VS Code / IntelliJ / PhpStorm

## 🧩 Structure du projet

LearnFlexPlus/
│
├── frontend/               # Interfaces utilisateur
├── backend/                # Logique métier / API / contrôleurs
├── database/               # Scripts SQL / schéma BD
├── assets/                 # Images, icônes, fichiers statiques
├── docs/                   # Documentation technique / UML
└── README.md               # Documentation principale
