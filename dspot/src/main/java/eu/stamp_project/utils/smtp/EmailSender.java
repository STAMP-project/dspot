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
	private String username;
	private String password;
	public EmailSender (String username, String password){
		this.username = username;
		this.password = password;
	}

    public void sendEmail(String messageText,String subject,String fromUser,String toUsers) {
        final String username = this.username;
        final String password = this.password;
        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
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
