import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

public class SendMessage extends HttpServlet {
    private Message message        = null;
    protected  static  String   SMTP_SERVER    = "smtp.mail.ru";
    protected  static  String   SMTP_AUTH_USER = null;
    protected  static  String   SMTP_AUTH_PWD  = null;
    protected  static  String   SMTP_Port      = "465";
    protected  static  String   EMAIL_FROM     = null;
    protected  static  String   REPLY_TO       = null;

    public void init(ServletConfig config){

    }

    public void config(String mailbox, String password){
        Properties properties = new Properties();
        properties.put("mail.smtp.host"               , SMTP_SERVER);
        properties.put("mail.smtp.port"               , SMTP_Port  );
        properties.put("mail.smtp.auth"               , "true"     );
        properties.put("mail.smtp.ssl.enable"         , "true"     );
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        SMTP_AUTH_USER = mailbox;
        SMTP_AUTH_PWD = password;
        try {
            Authenticator auth = new EmailAuthenticator(SMTP_AUTH_USER,
                    SMTP_AUTH_PWD);
            Session session = Session.getDefaultInstance(properties,auth);
            session.setDebug(false);

            InternetAddress email_from = new InternetAddress(SMTP_AUTH_USER);
            InternetAddress reply_to   = (REPLY_TO != null) ?
                    new InternetAddress(REPLY_TO) : null;
            message = new MimeMessage(session);
            message.setFrom(email_from);
            message.setSubject("Don't panic. It's lab");
            if (reply_to != null)
                message.setReplyTo (new Address[] {reply_to});
        } catch (AddressException e) {
            System.err.println(e.getMessage());
        } catch (MessagingException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String destination = req.getParameter("destination");
        String messageText = req.getParameter("message");
        String mail = req.getServletContext().getInitParameter("mailbox");
        String password = req.getServletContext().getInitParameter("password");
        config(mail, password);

        InternetAddress email_to   = null;
        try {
            email_to = new InternetAddress(destination);
        } catch (AddressException e) {
            System.out.println(e.getMessage());
        }

        boolean result = false;
        try {
            message.setRecipient(Message.RecipientType.TO, email_to);
            // Содержимое сообщения
            Multipart mmp = new MimeMultipart();
            // Текст сообщения
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(messageText, "text/plain; charset=utf-8");
            mmp.addBodyPart(bodyPart);
            // Отправка сообщения
            Transport.send(message);
            result = true;
        } catch (MessagingException e){
            // Ошибка отправки сообщения
            System.out.println(e.getMessage());
        }
        resp.getWriter().println(result);
    }
}
