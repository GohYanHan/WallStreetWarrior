import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.Preferences;


class Notification {
    double updatedStockPrice;
    static boolean notificationSendSetting = true; //default true
    private double thresholds = 0; //default null

    //static Stock stock = new Stock(order.getSymbol());
    private final Database db = new Database();
    private final User user = db.getUser();
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

    public void sendNotification(int caseSymbol, String userEmail, Order order){
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
                message.setFrom(new InternetAddress(userEmail));
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
                            message.setText("Your have successfully purchased " + (order.getStock().getSymbol()) +" at a price of RM" + (order.getExpectedBuyingPrice())+ " for " + (order.getShares())+" shares."  );
                        break;
                    case 4: // when place sell order
                            message.setText("Your have successfully placed sell order of " + (order.getStock().getSymbol()) +" at a price of RM" + (order.getExpectedSellingPrice())+ " for " + (order.getShares())+" shares."  );
                        break;

                    case 5: //when sell order bought by others
                            message.setText("Your have successfully sold " + (order.getStock().getSymbol()) +" at a price of RM" + (order.getExpectedSellingPrice())+ " for " + (order.getShares())+" shares."  );
                        break;
                    case 6:
                        BodyPart attachment2 = new MimeBodyPart();
                        File pdfFile = new File(System.getProperty("user.home") + "/Downloads/" + user.getUsername() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
                        attachment2.setDataHandler(new DataHandler(new FileDataSource(pdfFile)));
                        attachment2.setFileName("UserReport.pdf"); // Set the desired file name for the attachment

                        BodyPart emailText = new MimeBodyPart();
                        emailText.setText(" ");

                        Multipart multipartContent = new MimeMultipart();
                        multipartContent.addBodyPart(attachment2);
                        multipartContent.addBodyPart(emailText);

                        message.setContent(multipartContent);
                }
                Transport.send(message);

                if (caseSymbol != 5)
                    System.out.println("Email sent successfully. ");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
       /* Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Order> order =  database.loadOrders(user.getKey(), Order.Type.BUY);
                thresholds = user.getThresholds();
                double boughtPrice = order.price(); // bought price

                double currentPrice = 0;
                try {
                    currentPrice = api.getRealTimePrice(order.getStock().getSymbol()) * order.getShares();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                if ( boughtPrice - currentPrice >= thresholds ){
                    try {
                        sendNotification(1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if (currentPrice - boughtPrice >= thresholds ){
                    try {
                        sendNotification(2);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


                //get the price of the stock in real time
                //get the price of the stock bought
                //compare the two prices
                //if the differences crosses the threshold then we send an email
                //
                System.out.println("Command executed");
            }
        }, 0, 1000); // 1000 milliseconds = 1 second
*/

    }

}


    /*
    public boolean setThresholdPrice(int userKey, double thresholdPrice, String stockName) {
        //set in db

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO thresholdTable (userKey, thresholdPrice, stockName) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userKey);
            statement.setDouble(2, thresholdPrice);
            statement.setString(3, stockName);

            // Execute the update statement
            int rowsUpdated = statement.executeUpdate();
            statement.close();
            return rowsInserted > 0;
         catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
    */
    /*
    public void getThresholdPrice(int userKey, String stockName){
        //get the threshhold price based on stockname and userkey
        //can be modified if only want based on userkey

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT thresholdPrice FROM lotpool WHERE stockName = ? AND userKey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, stockName);
            statement.setInt(2, userKey);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Stock stock = new Stock(resultSet.getString("symbol"), resultSet.getString("name"));
                lotpool.put(stock, resultSet.getInt("share"));
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    */
// get all stocks that is owned by the user // Userid
// check for the threshold for any of the stocks owned // i assume this is under some class either(UserAuthetication)
// Where do we store User stocks
// send a notification everytime the threshold has been reached // You need to have the stock info from the market