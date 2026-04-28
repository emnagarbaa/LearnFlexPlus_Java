package org.example.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST     = "smtp.gmail.com";
    private static final int    SMTP_PORT     = 587;
    private static final String FROM_EMAIL    = "emnagarbaa200@gmail.com";      // ← votre Gmail
    private static final String FROM_PASSWORD = "vkduvjpzxrzlybvg";           // ← mot de passe app 16 car (sans espaces)

    public static void envoyerFeedback(String toEmail, String titreExamen) {

        System.out.println("📧 Tentative d'envoi email à : " + toEmail);
        System.out.println("📧 Titre examen : " + titreExamen);

        Properties props = new Properties();
        props.put("mail.smtp.auth",                "true");
        props.put("mail.smtp.starttls.enable",     "true");
        props.put("mail.smtp.starttls.required",   "true");
        props.put("mail.smtp.host",                SMTP_HOST);
        props.put("mail.smtp.port",                String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust",           "smtp.gmail.com");
        props.put("mail.debug",                    "true");   // ← affiche les logs SMTP dans la console

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Nouveau feedback sur votre examen");
            message.setText(
                    "Bonjour,\n\n" +
                            "Un feedback a ete ajoute sur l'examen : « " + titreExamen + " ».\n\n" +
                            "Connectez-vous a la plateforme pour consulter le commentaire de votre enseignant.\n\n" +
                            "Cordialement,\n" +
                            "L'equipe de la Plateforme d'Evaluation"
            );

            Transport.send(message);
            System.out.println("✅ Email envoye avec succes a : " + toEmail);

        } catch (AuthenticationFailedException e) {
            System.err.println("❌ Erreur d'authentification Gmail :");
            System.err.println("   → Vérifiez FROM_EMAIL et FROM_PASSWORD");
            System.err.println("   → Utilisez un mot de passe d'application (16 caractères)");
            System.err.println("   → Détail : " + e.getMessage());
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
        }
    }
}