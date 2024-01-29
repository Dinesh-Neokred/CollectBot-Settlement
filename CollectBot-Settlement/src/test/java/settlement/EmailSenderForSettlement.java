package settlement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class EmailSenderForSettlement {

    public void sendMailWithAttachment(String path, String mail, String pass, String date)
            throws EncryptedDocumentException, IOException, AddressException {

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate todayDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate yesterdayDate = yesterday.atZone(ZoneId.systemDefault()).toLocalDate();
        // System.out.println(now);
        String yesterdayDateWithoutTime = yesterdayDate.format(formatter);
        System.out.println(yesterdayDate);

        String filePath = path;

        File dailyReportFile = new File(filePath);

        String dailyReport = dailyReportFile.getAbsolutePath();
        // EmailSender email = new EmailSender();

        String email = mail;
        String passwordMail = pass;

        // Recipient's email address
        String to = "ganesh@neokred.tech";

        // Sender's email address
        String from = email;

        // SMTP server details
        // SMTP server details
        String host = "smtp.office365.com";
        String port = "587";

        final String username = email;
        final String password = pass;

        String cc1 = "karna@neokred.tech";
        String cc2 = "accounts@neokred.tech";
        String cc3 = "sanjay@neokred.tech";
        String cc4 = "chandana@neokred.tech";

        Address ccAddress1 = new InternetAddress(cc1);
        Address ccAddress2 = new InternetAddress(cc2);
        Address ccAddress3 = new InternetAddress(cc3);
        Address ccAddress4 = new InternetAddress(cc4);

        // Create properties object
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.starttls.enable", "true");

        // Create session object
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create message object
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.addRecipient(Message.RecipientType.CC, ccAddress1);
            message.addRecipient(Message.RecipientType.CC, ccAddress2);
            message.addRecipient(Message.RecipientType.CC, ccAddress3);
            message.addRecipient(Message.RecipientType.CC, ccAddress4);

            // Set email subject
            message.setSubject("Settlement Record is Created For the date " + date);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Set the message content
            messageBodyPart.setText("Hi Ganesh,\n"
                    + "\n"
                    + "The Settlement Details is updated in The Dashboard as per the sheet which you have shared using automation scripts.Please check from your end "
                    + "\n\n"
                    + "Thank You");

            // Create the attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();

            // Set the attachment file

            DataSource source = new FileDataSource(dailyReport);
            attachmentPart.setDataHandler(new DataHandler(source));
            String attachmentFileName = date + " SettlementSheet.xlsx";
            attachmentPart.setFileName(attachmentFileName);

            // attachmentPart.setFileName(dailyReport);

            // Create the multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Set the complete message parts
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
