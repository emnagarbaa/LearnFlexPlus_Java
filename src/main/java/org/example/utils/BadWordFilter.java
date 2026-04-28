package org.example.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Filtre de mots inappropriés pour LearnFlex+
 * - Détecte les mots interdits dans les publications et commentaires
 * - Masque les mots détectés avec des étoiles
 * - Envoie un email d'avertissement à l'utilisateur
 */
public class BadWordFilter {

    // =====================================================
    //   LISTE DES MOTS INTERDITS
    //   Ajoutez vos mots ici (français + anglais)
    // =====================================================
    private static final List<String> MOTS_INTERDITS = Arrays.asList(
            // Insultes françaises
            "idiot", "imbécile", "con", "connard", "connasse",
            "abruti", "crétin", "débile", "nul", "nulle",
            "stupide", "imbecile", "salaud", "salope", "pute",
            "enculé", "enculer", "merde", "putain", "bordel",
            "bâtard", "batard", "tabarnak", "ostie", "câlice",
            "cul", "bite", "couille", "couilles", "chier",
            "foutre", "niquer", "nique", "baiser", "encule",
            "fdp", "fils de pute", "va te faire",

            // Insultes anglaises
            "idiot", "stupid", "moron", "fool", "loser",
            "bitch", "bastard", "asshole", "ass", "damn",
            "shit", "fuck", "fucking", "fucker", "crap",
            "hell", "wtf", "stfu", "kys",

            // Discriminatoires / haineux
            "raciste", "nazi", "fasciste", "terroriste",
            "haine", "tuer", "mort", "suicide", "viol",

            // Spam / hors contexte
            "spam", "pub gratuite", "argent facile", "casino"
    );

    // =====================================================
    //   CONFIGURATION EMAIL (Gmail SMTP)
    //   Remplacez par vos vraies coordonnées
    // =====================================================
    private static final String SMTP_HOST     = "smtp.gmail.com";
    private static final int    SMTP_PORT     = 587;
    private static final String SMTP_USER     = "aouamriamal0000@gmail.com";     // ← Votre email Gmail
    private static final String SMTP_PASSWORD = "azyb eqsd kkse plxs";    // ← Mot de passe d'application Gmail

    // =====================================================
    //   DÉTECTER UN MOT INTERDIT
    //   Retourne le mot interdit trouvé, ou null si aucun
    // =====================================================
    public static String detecterMotInterdit(String texte) {
        if (texte == null || texte.isEmpty()) return null;

        String texteLower = texte.toLowerCase()
                .replace("@", "a")
                .replace("0", "o")
                .replace("1", "i")
                .replace("3", "e")
                .replace("$", "s")
                .replace("5", "s");

        for (String mot : MOTS_INTERDITS) {
            // Vérifier si le mot apparaît en tant que mot entier
            String pattern = "(?i)(?<![a-zA-ZÀ-ÿ])" + mot + "(?![a-zA-ZÀ-ÿ])";
            if (texteLower.matches(".*" + pattern + ".*")) {
                return mot;
            }
        }
        return null;
    }

    // =====================================================
    //   MASQUER UN MOT
    //   ex: "merde" → "m***e"
    // =====================================================
    public static String masquerMot(String mot) {
        if (mot == null || mot.length() <= 2) return "***";
        StringBuilder sb = new StringBuilder();
        sb.append(mot.charAt(0));
        for (int i = 1; i < mot.length() - 1; i++) {
            sb.append('*');
        }
        sb.append(mot.charAt(mot.length() - 1));
        return sb.toString();
    }

    // =====================================================
    //   ENVOYER UN EMAIL D'AVERTISSEMENT (asynchrone)
    // =====================================================
    public static void envoyerAvertissement(String emailDestinataire,
                                            String prenomUtilisateur,
                                            String motInterdit,
                                            String contexte) {
        // Exécuté dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
                props.put("mail.smtp.ssl.trust", SMTP_HOST);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USER, "LearnFlex+ Modération"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(emailDestinataire));
                message.setSubject("⚠️ Avertissement — Contenu inapproprié détecté");

                String corps =
                        "<html><body style='font-family:Arial,sans-serif;'>" +
                                "<div style='background:#f8f9fa;padding:30px;border-radius:10px;max-width:600px;margin:auto;'>" +
                                "<div style='background:#e74c3c;padding:15px;border-radius:8px;text-align:center;'>" +
                                "<h2 style='color:white;margin:0;'>⚠️ Avertissement LearnFlex+</h2>" +
                                "</div>" +
                                "<div style='padding:20px;background:white;border-radius:8px;margin-top:15px;'>" +
                                "<p>Bonjour <strong>" + prenomUtilisateur + "</strong>,</p>" +
                                "<p>Nous avons détecté l'utilisation d'un mot inapproprié dans votre <strong>"
                                + contexte + "</strong> sur LearnFlex+.</p>" +
                                "<div style='background:#ffeaa7;border-left:4px solid #e74c3c;padding:10px 15px;border-radius:4px;margin:15px 0;'>" +
                                "<p style='margin:0;'><strong>Mot détecté :</strong> " +
                                masquerMot(motInterdit) + "</p>" +
                                "</div>" +
                                "<p>Merci de respecter les règles de la communauté LearnFlex+ :</p>" +
                                "<ul>" +
                                "<li>Utiliser un langage respectueux</li>" +
                                "<li>Éviter les insultes et propos discriminatoires</li>" +
                                "<li>Contribuer positivement à la communauté</li>" +
                                "</ul>" +
                                "<p>En cas de récidive, votre compte pourra être suspendu.</p>" +
                                "<p style='color:#95a5a6;font-size:12px;margin-top:20px;'>" +
                                "Cet email a été envoyé automatiquement par le système de modération LearnFlex+.</p>" +
                                "</div></div></body></html>";

                message.setContent(corps, "text/html; charset=UTF-8");
                Transport.send(message);

                System.out.println("[BadWordFilter] Email d'avertissement envoyé à : " + emailDestinataire);

            } catch (Exception e) {
                System.err.println("[BadWordFilter] Erreur envoi email : " + e.getMessage());
            }
        }).start();
    }
}
