package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "f522a319a791f5"; // Mailtrap username
    private final String password = "a63037e7c67847"; // Mailtrap password

    public void sendApprovalEmail(String recipient, String date, String time, double price) throws MessagingException {
        String subject = "Confirmation de votre consultation";
        String body = String.format(
                "<!DOCTYPE html>" +
                        "<html lang='fr'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<style>" +
                        "body { font-family: 'Arial', sans-serif; background-color: #f4f7fa; margin: 0; padding: 0; }" +
                        ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }" +
                        ".header { background-color: #007bff; color: #ffffff; padding: 20px; text-align: center; }" +
                        ".header h1 { margin: 0; font-size: 24px; }" +
                        ".content { padding: 30px; }" +
                        ".content p { font-size: 16px; color: #333333; line-height: 1.6; }" +
                        ".details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                        ".details p { margin: 10px 0; font-size: 16px; }" +
                        ".details strong { color: #007bff; }" +
                        ".footer { background-color: #f1f3f5; padding: 20px; text-align: center; }" +
                        ".footer p { font-size: 14px; color: #666666; margin: 0; }" +
                        ".button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; margin-top: 20px; }" +
                        "@media only screen and (max-width: 600px) { .content { padding: 20px; } .header h1 { font-size: 20px; } }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>Confirmation de votre consultation</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Cher patient,</p>" +
                        "<p>Votre consultation a été confirmée avec succès. Voici les détails :</p>" +
                        "<div class='details'>" +
                        "<p><strong>Date :</strong> %s</p>" +
                        "<p><strong>Heure :</strong> %s</p>" +
                        "<p><strong>Prix :</strong> %.2f TND</p>" +
                        "</div>" +
                        "<p>Merci de vous présenter 15 minutes avant l'heure prévue.</p>" +

                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Cordialement,<br>L'équipe E-Health</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                date, time, price
        );
        sendEmail(recipient, subject, body);
    }

    public void sendRejectionEmail(String recipient) throws MessagingException {
        String subject = "Annulation de votre consultation";
        String body = "<!DOCTYPE html>" +
                "<html lang='fr'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: 'Arial', sans-serif; background-color: #f4f7fa; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }" +
                ".header { background-color: #dc3545; color: #ffffff; padding: 20px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".content p { font-size: 16px; color: #333333; line-height: 1.6; }" +
                ".footer { background-color: #f1f3f5; padding: 20px; text-align: center; }" +
                ".footer p { font-size: 14px; color: #666666; margin: 0; }" +
                ".button { display: inline-block; padding: 12px 24px; background-color: #dc3545; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; margin-top: 20px; }" +
                "@media only screen and (max-width: 600px) { .content { padding: 20px; } .header h1 { font-size: 20px; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Annulation de votre consultation</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Cher patient,</p>" +
                "<p>Nous regrettons de vous informer que votre consultation a été annulée.</p>" +
                "<p>Pour plus d'informations, veuillez contacter notre secrétariat.</p>" +

                "</div>" +
                "<div class='footer'>" +
                "<p>Cordialement,<br>L'équipe E-Health</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        sendEmail(recipient, subject, body);
    }

    private void sendEmail(String recipient, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "2525");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("no-reply@clinique.tn"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        Transport.send(message);
    }
}