package eu.stamp_project.utils.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Used to collect output information from selector class.
public class SmtpConfig {

    /* Initialization */
    private static SmtpConfig smtpConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpConfig.class);

    public static SmtpConfig getInstance() {
        if (smtpConfig == null ) {
            smtpConfig = new SmtpConfig();
        }
        return smtpConfig;
    }

    /* Variables and getters/setters */
    private static String smtpUserName;
    private static String smtpPassword;
    private static String smtpHost;
    private static String smptPort;
    private static String smtpAuth;
    private static String smtpTls;

    public void setSmtpUserName(String smtpUserName) {
        this.smtpUserName = smtpUserName;
    }

    public String getSmtpUserName() {
        return this.smtpUserName;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getSmtpPassword() {
        return this.smtpPassword;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpHost() {
        return this.smtpHost;
    }

    public void setSmtpPort(String smptPort) {
        this.smptPort = smptPort;
    }

    public String getSmtpPort() {
        return this.smptPort;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpAuth() {
        return this.smtpAuth;
    }

    public void setSmtpTls(String smtpTls) {
        this.smtpTls = smtpTls;
    }

    public String getSmtpTls() {
        return this.smtpTls;
    }
}
