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
    private static final SmtpConfig smtpConfig = SmtpConfig.getInstance();
    private static EmailSender emailSender;
    private static boolean emailSendedWithoutException = true; // use for testing later.

	private String smtpUsername;
	private String smtpPassword;
    private String smtpHost;
    private String smtpPort;
    private String smtpAuth;
    private String smtpTls;

    public static EmailSender getInstance() {
        if (emailSender == null) {
            emailSender = new EmailSender();
        }
        return emailSender;
    }

    public void sendEmail(String messageText,String subject,String toUsers) {
        final String username = this.smtpConfig.getSmtpUserName();
        final String password = this.smtpConfig.getSmtpPassword();
        final String fromUser = username;

        Properties prop = new Properties();
		prop.put("mail.smtp.host", this.smtpConfig.getSmtpHost());
        prop.put("mail.smtp.port", this.smtpConfig.getSmtpPort());
        prop.put("mail.smtp.auth", this.smtpConfig.getSmtpAuth());
        prop.put("mail.smtp.starttls.enable", this.smtpConfig.getSmtpTls()); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username,password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(fromUser));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(toUsers));

            // Set Subject: header field
            message.setSubject(subject);

            message.setText(messageText);

            Transport.send(message);

            LOGGER.warn("Done sending files");

            this.emailSendedWithoutException = true;
        } catch (MessagingException e) {
            e.printStackTrace();
            this.emailSendedWithoutException = false;
        }
    }

    /* Use for testing */
    public boolean checkIfEmailSendedWithoutException() {
        return this.emailSendedWithoutException;
    }

    public void send(){}
}
