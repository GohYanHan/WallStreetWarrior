import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


class Notification {
    private double updatedStockPrice;
    static boolean notificationSendSetting = true; //default true
    private double thresholdProfit = 0; //default null
    private double thresholdLoss = 0; //default null
    private double thresholdPrice = 0;
    private static double boughtPrice; // This attribute should fall under Orders or Portfolio?
    private double profitLossPerStock; // should = boughtPrice - currentPrice of Stock
    User user;
    Stock stock = new Stock();
    Order order = new Order();
    API api = new API();
    TradingEngine tradingEngine;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private void setThresholdProfit(User user, double threshholdProfit) {
        this.thresholdProfit = threshholdProfit;
    }

    ;

    private void setThresholdLoss(Integer input) {
        this.thresholdLoss = input;
    }

    ;

    private void setProfitLossPerStock() {
    }

    ;

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


    //on button click do set true or set false, default true, assuming there are 2 buttons(enable/disable)
    public boolean setNotificationSendSettingTrue() {
        System.out.println("Notification setting has been turned on.");
        return notificationSendSetting = true;
    }

    public boolean setNotificationSendSettingFalse() {
        System.out.println("Notification setting has been turned off.");
        return notificationSendSetting = false;
    }


    public void start() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Method to be called every 1 second
                try {
                    updateStockPrice(order);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Schedule the task to run every 1 second
        timer.schedule(task, 0, 1000);
    }

    public void updateStockPrice(Order order) throws IOException {
        updatedStockPrice = api.getRealTimePrice(order.getStock().getSymbol());
        this.updatedStockPrice = updatedStockPrice;
        System.out.println("Stock price updated: " + updatedStockPrice);
        if (updatedStockPrice >= 0) {
            if (updatedStockPrice > thresholdPrice) {
                sendNotification(1, order.getStock());
            } else if (updatedStockPrice < thresholdPrice) {
                sendNotification(2, order.getStock());
            }
        }

    }
    //handled by timer, every XX call this method

    public void sendNotification(int caseSymbol, Stock stock) {
        //initialising
        String emailscol = "";
        String subject = "";
        String body = "";

        switch (caseSymbol) {
            case 1: //(updatedStockPrice > thresholdPrice)==true
                body = "Your stock " + order.getType() + " has a profit of " + (thresholdPrice - updatedStockPrice);

            case 2: //(updatedStockPrice < thresholdPrice)==true
                body = "Your stock " + order.getType() + " has a loss of " + (updatedStockPrice - thresholdPrice);

            case 3: //when buy order
                body = "Notification: Thank you for buying. ";

            case 4: //when sell order
                body = "Notification: Stock has been sold. ";
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wsw1", "root", "abc123")) {
            // Fetch email details from the database
            String sql = "SELECT emailscol, subject, body FROM emails";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                // Iterate over the result set
                while (resultSet.next()) {
                    // Get the email details from the result set
                    emailscol = resultSet.getString("emailscol");
                    subject = resultSet.getString("subject");
                    body = resultSet.getString("body");

                    // Use JavaMail API to send the email
                    // Set the recipient email, subject, body, etc.
                    // Send the email using SMTP server or other configuration
                    //sendEmail(emailscol, subject, body);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public void sendNotificationEnter() throws IOException { //when buy
        if (notificationSendSetting = true) {
            //send email
            while (thresholdPrice != updatedStockPrice) {
                updateStockPrice(order);
            }
        }
    }

    public void sendNotificationExit() { //when sell
        if (notificationSendSetting = true) {
            //send email
        }
    }
}

class SendEmail {

    public static void main(String[] args) {
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
            recipients[0] = new InternetAddress("sornphert03@gmail.com");

            message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sornphert03@gmail.com"));
            message.addRecipients(Message.RecipientType.TO, recipients);
            message.setSubject("Testing");
            message.setText("This is the first test email. ");

            Transport.send(message);

            System.out.println("Email sent");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
}