import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.prefs.Preferences;

class Notification {
    static boolean notificationSendSetting = true; //default true
    private final Database db = new Database();
    private User user = db.getUser();
    double thresholds = 0; //default null

    //on button click do set true or set false, default true, assuming there are 2 buttons(enable/disable)
    public boolean setNotificationSendSettingTrue() {
        System.out.println("Notification setting has been turned on.");
        return notificationSendSetting = true;
    }

    public boolean setNotificationSendSettingFalse() {
        System.out.println("Notification setting has been turned off.");
        return notificationSendSetting = false;
    }

    private static final String NOTIFICATION_PREF_KEY = "notificationSetting";
    private static Preferences preferences = Preferences.userNodeForPackage(Notification.class);

    public static boolean isNotificationSendSetting() {
        return preferences.getBoolean(NOTIFICATION_PREF_KEY, true);
    }

    public static void setNotificationSendSetting(boolean value) {
        preferences.putBoolean(NOTIFICATION_PREF_KEY, value);
    }

    public static void loadNotificationSettings() {
        // Load the notification setting from Preferences
        notificationSendSetting = preferences.getBoolean(NOTIFICATION_PREF_KEY, true);
    }

    public static void saveNotificationSettings() {
        // Save the notification setting to Preferences
        preferences.putBoolean(NOTIFICATION_PREF_KEY, notificationSendSetting);
    }

    public double getThresholds(User user) {
        return user.getThresholds();
    }

    public void sendNotification(int caseSymbol, String userEmail, Order order) {
        double thresholds = getThresholds(order.getUser());

        if (notificationSendSetting) {
            Properties props;
            Session session;
            MimeMessage message;

            props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("sornphert03@gmail.com", "hhftdeernmxqlnaq");
                }
            };
            session = Session.getInstance(props, auth);

            try {
                InternetAddress[] recipients = new InternetAddress[1];
                recipients[0] = new InternetAddress(userEmail);

                message = new MimeMessage(session);
                message.setFrom(new InternetAddress("sornphert03@gmail.com"));
                message.addRecipients(Message.RecipientType.TO, recipients);
                message.setSubject("Stock Trading Activities");

                switch (caseSymbol) {
                    case 1:
                        message.setText("Your stock " + order.getStock().getSymbol() + " has made a profit of RM " + thresholds);
                        break;
                    case 2:
                        message.setText("Your stock " + order.getStock().getSymbol() + " has incurred a loss of RM " + thresholds);
                        break;
                    case 3: //when successfully execute buy order
                        message.setText("Your have successfully purchased " + (order.getStock().getSymbol()) + " at a price of RM" + (order.getExpectedBuyingPrice()) + " for " + (order.getShares()) + " shares.");

                        scheduleNotificationJob();

                        break;
                    case 4: // when place sell order
                        message.setText("Your have successfully placed sell order of " + (order.getStock().getSymbol()) + " at a price of RM" + (order.getExpectedSellingPrice()) + " for " + (order.getShares()) + " shares.");

                        break;

                    case 5: //when sell order bought by others
                        message.setText("Your have successfully sold " + (order.getStock().getSymbol()) + " at a price of RM" + (order.getExpectedSellingPrice()) + " for " + (order.getShares()) + " shares.");
                        break;
                    case 6:
                        Report report = new Report();
                        String reportFilePath = report.generateReport(); // Generate the report and retrieve the file path

                        if (reportFilePath != null) {
                            try {
                                BodyPart attachment2 = new MimeBodyPart();
                                File pdfFile = new File(reportFilePath); // Use the retrieved file path
                                attachment2.setDataHandler(new DataHandler(new FileDataSource(pdfFile)));
                                attachment2.setFileName("UserReport.pdf"); // Set the desired file name for the attachment

                                BodyPart emailText = new MimeBodyPart();
                                emailText.setText(" ");

                                Multipart multipartContent = new MimeMultipart();
                                multipartContent.addBodyPart(attachment2);
                                multipartContent.addBodyPart(emailText);

                                message.setContent(multipartContent); // Set the multipartContent as the message content
                                System.out.println("User report generated successfully in the Downloads folder.\n");
                            } catch (MessagingException e) {
                                System.out.println("An error occurred while sending the email.");
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("An error occurred while generating the user report. Unable to send email.");
                        }
                        break;
                }
                Transport.send(message);

                if (caseSymbol != 5)
                    System.out.println("Email sent successfully. ");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    //for FraudDetection only
    public void sendNotificationToAdmin(String userEmail, User suspiciousUser) {
        Properties props;
        Session session;
        MimeMessage message;

        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sornphert03@gmail.com", "hhftdeernmxqlnaq");
            }
        };

        session = Session.getInstance(props, auth);

        try {
            InternetAddress[] recipients = new InternetAddress[1];
            recipients[0] = new InternetAddress(userEmail);

            message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userEmail));
            message.addRecipients(Message.RecipientType.TO, recipients);
            message.setSubject("Suspicious User");

            // Build the body of the message
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("A suspicious user has been detected.\n\n");
            bodyBuilder.append("Name: ").append(suspiciousUser.getUsername()).append("\n");
            bodyBuilder.append("Email: ").append(suspiciousUser.getEmail()).append("\n\n");
            bodyBuilder.append("Transaction History:\n");

            // Append each transaction to the body
            for (Order order : db.loadTransactionHistory(suspiciousUser.getKey())) {
                bodyBuilder.append("Stock Symbol: ").append(order.getStock().getSymbol()).append("\n");
                bodyBuilder.append("Stock Name: ").append(order.getStock().getName()).append("\n");
                bodyBuilder.append("Type:  ").append(order.getType()).append("\n");
                bodyBuilder.append("Shares: ").append(order.getShares()).append("\n");
                if (order.getType() == Order.Type.BUY)
                    bodyBuilder.append("Price: ").append(order.getExpectedBuyingPrice()).append("\n");
                else
                    bodyBuilder.append("Price: ").append(order.getExpectedSellingPrice()).append("\n");


                bodyBuilder.append("Date: ").append(order.getTimestamp()).append("\n\n");
            }

            message.setText(bodyBuilder.toString());

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void scheduleNotificationJob() {
        try {
            // Create a Quartz scheduler
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();

            // Define the job and tie it to the NotificationJob class
            JobDetail job = JobBuilder.newJob(NotificationJob.class)
                    .withIdentity("notificationJob", "group1")
                    .build();

            // Pass the thresholds value through the job's JobDataMap
            job.getJobDataMap().put("thresholds", thresholds);

            // Create a trigger that fires every second
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("notificationTrigger", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();

            // Schedule the job with the trigger
            scheduler.scheduleJob(job, trigger);

            // Start the scheduler
            scheduler.start();

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
        /*public void scheduleNotificationJob(String stockSymbol) {
        try {
            // Define the job and tie it to the  class
            JobDetail job = JobBuilder.newJob(NotificationJob.class)
                    .withIdentity(stockSymbol, "notificationJob") // Use stock symbol as the job name and "notificationJob" as the group
                    .usingJobData("stockSymbol", stockSymbol) // Pass stock symbol as job data
                    .build();

            // Create a new trigger with a unique name and group
            String triggerGroup = stockSymbol + "_triggerGroup"; // Use a unique trigger group based on stock symbol
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(stockSymbol + "_trigger", triggerGroup) // Use a unique trigger name based on stock symbol and the trigger group
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();

            // Schedule the job with the trigger
            System.out.println("Job scheduled for stock symbol: " + stockSymbol);
            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }*/
}
