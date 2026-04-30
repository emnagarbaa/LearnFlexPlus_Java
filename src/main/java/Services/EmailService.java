package Services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.UnsupportedEncodingException;

public class EmailService {

    // ── Configure these ──────────────────────────────────────────────────────
    private static final String SMTP_HOST      = "smtp.gmail.com";
    private static final int    SMTP_PORT      = 587;
    private static final String SENDER_EMAIL   = "omar.jomni433@gmail.com";
    private static final String SENDER_PASSWORD = "ekgg trem wjwl elee";   // Gmail App Password
    // ─────────────────────────────────────────────────────────────────────────

    public static void sendVerificationEmail(String toEmail, String code) throws MessagingException, UnsupportedEncodingException  {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL, "LearnFlex+"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("LearnFlex+ – Code de vérification");
        msg.setContent(buildHtml(code), "text/html; charset=utf-8");
        Transport.send(msg);
    }

    private static String buildHtml(String code) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:520px;margin:auto;">
              <div style="background:#1f4f65;padding:24px;border-radius:12px 12px 0 0;text-align:center;">
                <h2 style="color:white;margin:0;">LearnFlex+</h2>
              </div>
              <div style="border:1px solid #ddd;border-top:none;padding:32px;border-radius:0 0 12px 12px;">
                <h3 style="color:#1f4f65;">Vérification de votre compte</h3>
                <p style="color:#444;">Entrez ce code dans l'application :</p>
                <div style="font-size:38px;font-weight:bold;color:#1f4f65;letter-spacing:10px;
                            background:#f0f6ff;padding:18px;border-radius:8px;text-align:center;">
                  %s
                </div>
                <p style="color:#999;font-size:12px;margin-top:20px;">
                  Ce code expire dans <strong>10 minutes</strong>. Ne le partagez jamais.
                </p>
              </div>
            </body></html>
            """.formatted(code);
    }
}