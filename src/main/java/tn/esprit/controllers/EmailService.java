package tn.esprit.controllers;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private final String username;
    private final String password;
    private final Properties smtpProps;

    public EmailService() {
        // Charger les propriétés depuis application.properties
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier application.properties introuvable");
            }
            props.load(input);
            this.username = props.getProperty("email.username");
            this.password = props.getProperty("email.password");
            this.smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", props.getProperty("email.smtp.auth"));
            smtpProps.put("mail.smtp.starttls.enable", props.getProperty("email.smtp.starttls"));
            smtpProps.put("mail.smtp.host", props.getProperty("email.smtp.host"));
            smtpProps.put("mail.smtp.port", props.getProperty("email.smtp.port"));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement des propriétés : " + e.getMessage());
        }
    }

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
        // Créer une session avec authentification
        Session session = Session.getInstance(smtpProps, new Authenticator() {
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