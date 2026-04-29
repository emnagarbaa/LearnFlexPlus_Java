package org.example.Services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class ServiceEmail {

    // Configuration SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_EXPEDITEUR = "emnagarbaa200@gmail.com";
    private static final String MOT_DE_PASSE = "vkduvjpzxrzlybvg";

    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            // Utiliser setText au lieu de setContent pour éviter les problèmes MIME
            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email envoyé à " + destinataire);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Email spécifique pour la correction d'examen (version texte)
    public void envoyerCorrectionExamen(String emailEtudiant, String nomEtudiant,
                                        String examenTitre, double note,
                                        String commentaire, double moyenne) {
        String sujet = "📝 Correction de votre examen - " + examenTitre;

        String contenu = String.format("""
            =========================================
            LearnFlex+ - Correction de votre examen
            =========================================
            
            Bonjour %s,
            
            Votre examen « %s » a été corrigé par votre professeur.
            
            -------------------------------------------------
            RÉSULTATS :
            -------------------------------------------------
            Note obtenue : %.1f / 20
            Moyenne de la classe : %.1f / 20
            
            -------------------------------------------------
            COMMENTAIRE DU PROFESSEUR :
            -------------------------------------------------
            %s
            
            -------------------------------------------------
            
            Pour plus de détails, connectez-vous à votre espace LearnFlex+.
            
            Cet email est un message automatique, merci de ne pas y répondre.
            
            © 2026 LearnFlex+ - Tous droits réservés
            =========================================
            """,
                nomEtudiant,
                examenTitre,
                note,
                moyenne,
                commentaire != null && !commentaire.isEmpty() ? commentaire : "Aucun commentaire"
        );

        envoyerEmail(emailEtudiant, sujet, contenu);
    }

    // Version HTML simplifiée (si vous voulez garder le HTML)
    public void envoyerCorrectionExamenHTML(String emailEtudiant, String nomEtudiant,
                                            String examenTitre, double note,
                                            String commentaire, double moyenne) {
        String sujet = "📝 Correction de votre examen - " + examenTitre;

        String contenu = String.format("""
            <html>
            <head><style>
                body { font-family: Arial, sans-serif; }
                .container { max-width: 600px; margin: auto; padding: 20px; }
                .header { background: #1f4f65; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; }
                .note { font-size: 36px; font-weight: bold; color: %s; }
                .comment { background: #f4f4f4; padding: 15px; margin: 15px 0; }
                .footer { background: #f4f8f7; padding: 15px; text-align: center; font-size: 12px; }
            </style></head>
            <body>
                <div class='container'>
                    <div class='header'>
                        <h2>LearnFlex+</h2>
                        <p>Correction de votre examen</p>
                    </div>
                    <div class='content'>
                        <h3>Bonjour %s,</h3>
                        <p>Votre examen <strong>« %s »</strong> a été corrigé.</p>
                        
                        <h3>📊 Résultats :</h3>
                        <p class='note'>%.1f / 20</p>
                        <p>📈 Moyenne de la classe : <strong>%.1f / 20</strong></p>
                        
                        <div class='comment'>
                            <h3>💬 Commentaire :</h3>
                            <p>%s</p>
                        </div>
                        
                        <p>Connectez-vous à LearnFlex+ pour plus de détails.</p>
                    </div>
                    <div class='footer'>
                        <p>© 2026 LearnFlex+ - Cet email est automatique</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                note >= 10 ? "#27ae60" : "#e74c3c",
                nomEtudiant,
                examenTitre,
                note,
                moyenne,
                commentaire != null && !commentaire.isEmpty() ? commentaire : "Aucun commentaire"
        );

        envoyerEmailHTML(emailEtudiant, sujet, contenu);
    }

    // Méthode pour envoyer du HTML avec gestion correcte du content type
    private void envoyerEmailHTML(String destinataire, String sujet, String contenuHTML) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet, "UTF-8");
            message.setContent(contenuHTML, "text/html; charset=UTF-8");

            // Sauvegarder les changements avant d'envoyer
            message.saveChanges();

            Transport.send(message);
            System.out.println("✅ Email HTML envoyé à " + destinataire);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email HTML : " + e.getMessage());
            e.printStackTrace();
        }
    }
}