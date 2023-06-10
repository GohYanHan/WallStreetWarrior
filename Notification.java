import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.Preferences;


class Notification {
    double updatedStockPrice;
    static boolean notificationSendSetting = true; //default true
    private double thresholds = 0; //default null

    //static Stock stock = new Stock(order.getSymbol());
    static User user = new User();
    private Database db = new Database();
    Report report = new Report();
    static API api = new API();
    Database database = new Database();

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

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


    public void sendNotification(int caseSymbol, String userEmail, Order order) {
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

            User user = database.getUser();


            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("sornphert03@gmail.com", "hhftdeernmxqlnaq");
                }
            };

            session = Session.getInstance(props, auth);

            try {

                InternetAddress[] recipients = new InternetAddress[1];
                recipients[0] = new InternetAddress(user.getEmail());

                message = new MimeMessage(session);
                message.setFrom(new InternetAddress("sornphert03@gmail.com"));
                message.addRecipients(Message.RecipientType.TO, recipients);
                message.setSubject("Stock Trading Activities");

                switch (caseSymbol) {
                    case 1:
                        message.setText("Your stock has made a profit of RM " + thresholds);
                        break;
                    case 2:
                        message.setText("Your stock has reached a loss of RM " + thresholds);
                        break;
                    case 3: //when successfully execute buy order
                        message.setText("Your have successfully purchased " + (order.getStock().getSymbol()) + " at a price of RM" + (order.getExpectedBuyingPrice()) + " for " + (order.getShares()) + " shares.");
                        break;
                    case 4: // when place sell order
                        message.setText("Your have successfully placed sell order of " + (order.getStock().getSymbol()) + " at a price of RM" + (order.getExpectedSellingPrice()) + " for " + (order.getShares()) + " shares.");
                        break;

                    case 5: //when sell order bought by others
                        //message.setText("Your have successfully sold " + (order.getStock().getSymbol()) +" at a price of RM" + (order.getExpectedSellingPrice())+ " for " + (order.getShares())+" shares."  );
                        break;

                    case 6:
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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                List<Order> orders = database.loadOrders(user.getKey(), Order.Type.BUY);
                thresholds = user.getThresholds();
                boolean notificationSent = false; // Flag variable to track notification status

                for (Order order : orders) {
                    if (notificationSent) {
                        break; // Exit the loop if a notification has been sent
                    }

                    double boughtPrice = order.getPrice(); // Bought price

                    double currentPrice = 0;
                    try {
                        currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (currentPrice >= boughtPrice + thresholds) {
                        sendNotification(1, user.getEmail(), order);
                        notificationSent = true; // Set the flag to true
                    } else if (currentPrice <= boughtPrice - thresholds) {
                        sendNotification(2, user.getEmail(), order);
                        notificationSent = true; // Set the flag to true
                    }
                }

                if (notificationSent) {
                    timer.cancel(); // Cancel the current timer
                    timer.schedule(this, 3600000); // Reschedule the timer after 1 hour (3600000 milliseconds)
                }

            }
        }, 0, 1000); // 1000 milliseconds = 1 second
    }
}