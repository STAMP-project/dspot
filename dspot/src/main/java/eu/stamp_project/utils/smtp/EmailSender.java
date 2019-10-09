package eu.stamp_project.utils.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender implements Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

	private String smtpUsername;
	private String smtpPassword;
    private String smtpHost;
    private String smtpPort;
    private boolean smtpAuth;
    private String smtpTls;

    public EmailSender(String smtpUsername,
                       String smtpPassword,
                       String smtpHost,
                       String smtpPort,
                       boolean smtpAuth,
                       String smtpTls) {
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpAuth = smtpAuth;
        this.smtpTls = smtpTls;
    }

    public void sendEmail(String messageText, String subject, String toUsers) {
        Properties prop = new Properties();
		prop.put("mail.smtp.host", this.smtpHost);
        prop.put("mail.smtp.port", this.smtpPort);
        prop.put("mail.smtp.auth", this.smtpAuth);
        prop.put("mail.smtp.starttls.enable", this.smtpTls); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername,smtpPassword);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(smtpUsername));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(toUsers));

            // Set Subject: header field
            message.setSubject(subject);

            message.setText(messageText);

            Transport.send(message);

            LOGGER.warn("Done sending files");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void send(){}
}
