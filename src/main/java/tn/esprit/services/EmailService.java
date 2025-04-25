package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "f522a319a791f5"; // Remplacez par votre username Mailtrap
    private final String password = "a63037e7c67847"; // Remplacez par votre password Mailtrap

    public void sendApprovalEmail(String recipient, String date, double price) throws MessagingException {
        String subject = "Confirmation de votre rendez-vous";
        String body = String.format(
                "Cher patient,\n\nVotre rendez-vous a été approuvé.\nDétails :\n- Date : %s\n- Prix : %.2f TND\n\nCordialement,\nVotre clinique",
                date, price
        );
        sendEmail(recipient, subject, body);
    }

    public void sendRejectionEmail(String recipient) throws MessagingException {
        String subject = "Annulation de votre rendez-vous";
        String body = "Cher patient,\n\nNous sommes désolés de vous informer que votre rendez-vous a été refusé.\nMerci de prendre un nouveau rendez-vous si nécessaire.\n\nCordialement,\nVotre clinique";
        sendEmail(recipient, subject, body);
    }

    private void sendEmail(String recipient, String subject, String body) throws MessagingException {
        // Configurer les propriétés du serveur SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "2525");
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 secondes
        props.put("mail.smtp.timeout", "10000"); // 10 secondes
        props.put("mail.debug", "true"); // Activer le débogage

        // Créer une session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Créer un message email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        // Envoyer l'email
        Transport.send(message);
    }
}