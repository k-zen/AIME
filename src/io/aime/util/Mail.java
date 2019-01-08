package io.aime.util;

// Log4j
import org.apache.log4j.Logger;

// Mail
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// Util
import java.util.Date;
import java.util.Properties;

/**
 * Esta clase maneja el envio de correos.
 *
 * @author K-Zen
 */
public class Mail {

    // Logs.
    private static final String KEY = Mail.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);

    /**
     * Este metodo envia un simple mensaje de texto.
     *
     * @param to      Destinatario
     * @param from    Recipiente
     * @param host    Servidor de correo.
     * @param subject Sujeto
     * @param message Mensaje a enviar.
     * @param timeout El timeout de conexion con el servidor SMTP.
     *
     * @throws MessagingException Si no es posible enviar el correo.
     */
    public void sendTextMail(String to, String from, String host, String subject, String message, int timeout) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.debug", "false");
        props.put("mail.smtp.timeout", timeout);

        Session session = Session.getInstance(props);
        Message msg = null;

        // Construir el mensaje.
        try {
            msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(message);
        }
        catch (MessagingException mex) {
            LOG.fatal("Error al construir el correo.", mex);
        }

        // Enviar el mensaje.
        Transport.send(msg);
    }
}
