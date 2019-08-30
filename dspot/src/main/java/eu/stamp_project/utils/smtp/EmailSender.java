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

	private String smtpUsername;
	private String smtpPassword;
    private String smtpHost;
    private String smtpPort;
    private String smtpAuth;
    private String smtpTls;

	public EmailSender (){
		this.smtpUsername = this.smtpConfig.getSmtpUserName();
		this.smtpPassword = this.smtpConfig.getSmtpPassword();
        this.smtpHost = this.smtpConfig.getSmtpHost();
        this.smtpPort = this.smtpConfig.getSmtpPort();
        this.smtpAuth = this.smtpConfig.getSmtpAuth();
        this.smtpTls = this.smtpConfig.getSmtpTls();
	}

    public void sendEmail(String messageText,String subject,String fromUser,String toUsers) {
        final String username = this.smtpUsername;
        final String password = this.smtpPassword;

        LOGGER.warn("SENDING EMAIL " + this.smtpPassword + " " + this.smtpUsername + " " + this.smtpHost + " " + this.smtpAuth);
        Properties prop = new Properties();
		prop.put("mail.smtp.host", this.smtpHost);
        prop.put("mail.smtp.port", this.smtpPort);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

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

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void send(){}
}
