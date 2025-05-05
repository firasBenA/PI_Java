package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    private final String senderEmail = "esprit.recover.plus@gmail.com";
    private final String senderPassword = "jxsdekiolyggrpjj";
    private final String smtpHost = "smtp.gmail.com";
    private final String smtpPort = "587";

    public boolean sendEmail(String recipientEmail, String subject, String reclamationSujet, String responseContent, String responseDate) {

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.debug", "true"); // Enable debug logging for troubleshooting

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("Nouvelle Réponse à Votre Réclamation: " + subject);

            // HTML email body
            String emailBody = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Réponse à Votre Réclamation</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <!-- Header -->
                        <tr>
                            <td style="background-color: #0288d1; padding: 20px; text-align: center; border-top-left-radius: 8px; border-top-right-radius: 8px;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 24px;">EHealth Support</h1>
                            </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                            <td style="padding: 20px;">
                                <h2 style="color: #333333; font-size: 20px; margin-top: 0;">Bonjour,</h2>
                                <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                    Une réponse a été ajoutée à votre réclamation. Voici les détails :
                                </p>
                                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0;">
                                    <tr>
                                        <td style="padding: 10px; background-color: #f9f9f9; border-left: 4px solid #0288d1;">
                                            <strong style="color: #333333;">Sujet de la réclamation :</strong><br>
                                            <span style="color: #666666;">{reclamationSujet}</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; background-color: #f9f9f9; border-left: 4px solid #0288d1;">
                                            <strong style="color: #333333;">Contenu de la réponse :</strong><br>
                                            <span style="color: #666666;">{responseContent}</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; background-color: #f9f9f9; border-left: 4px solid #0288d1;">
                                            <strong style="color: #333333;">Date de la réponse :</strong><br>
                                            <span style="color: #666666;">{responseDate}</span>
                                        </td>
                                    </tr>
                                </table>
                                <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                    Merci de votre confiance.
                                </p>
                                <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                    Cordialement,<br>
                                    L'équipe de support EHealth
                                </p>
                            </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                            <td style="background-color: #f4f4f4; padding: 10px; text-align: center; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;">
                                <p style="color: #999999; font-size: 12px; margin: 0;">
                                    © 2025 EHealth. Tous droits réservés.<br>
                                    Contactez-nous à <a href="mailto:support@ehealth.com" style="color: #0288d1; text-decoration: none;">support@ehealth.com</a>
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

            emailBody = emailBody.replace("{reclamationSujet}", escapeHtml(reclamationSujet))
                    .replace("{responseContent}", escapeHtml(responseContent))
                    .replace("{responseDate}", escapeHtml(responseDate));

            message.setContent(emailBody, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("Authentication failed for " + senderEmail + ": " + e.getMessage());
            System.err.println("Please verify the Gmail credentials (esprit.recover.plus@gmail.com) and ensure the app-specific password is correct.");
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + recipientEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}