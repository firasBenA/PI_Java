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
                "Cher patient,\n\n" +
                        "Votre consultation a été confirmée avec succès. Voici les détails :\n\n" +
                        "Date: %s\n" +
                        "Heure: %s\n" +
                        "Prix: %.2f TND\n\n" +
                        "Merci de vous présenter 15 minutes avant l'heure prévue.\n\n" +
                        "Cordialement,\nL'équipe médicale",
                date, time, price
        );
        sendEmail(recipient, subject, body);
    }

    public void sendRejectionEmail(String recipient) throws MessagingException {
        String subject = "Annulation de votre consultation";
        String body = "Cher patient,\n\n" +
                "Nous regrettons de vous informer que votre consultation a été annulée.\n\n" +
                "Pour plus d'informations, veuillez contacter notre secrétariat.\n\n" +
                "Cordialement,\nL'équipe médicale";
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
        message.setText(body);

        Transport.send(message);
    }
}