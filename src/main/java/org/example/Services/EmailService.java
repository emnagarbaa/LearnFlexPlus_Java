package org.example.Services;  // ✅ package correct

// ❌ Supprimer : import org.example.Services.EmailService;  (inutile)

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "moetezbedoui816@gmail.com";
    private static final String PASSWORD   = "xqto qdpk hura gheh";

    public static void sendNewEvenementEmail(String titre, String dateDebut, String lieu) {
        String toEmail = "moetezbedoui816@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Nouvel événement ajouté : " + titre);
            message.setText(
                    "Bonjour,\n\n" +
                            "Un nouvel événement a été ajouté :\n\n" +
                            "📌 Titre      : " + titre     + "\n" +
                            "📅 Date début : " + dateDebut + "\n" +
                            "📍 Lieu       : " + lieu      + "\n\n" +
                            "Cordialement."
            );

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès !");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}