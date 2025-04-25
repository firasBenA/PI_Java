package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "esprit.recover.plus@gmail.com";
    private final String password = "jxsdekiolyggrpjj";

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
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

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