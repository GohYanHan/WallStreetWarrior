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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


class Notification {
    double updatedStockPrice;
    static boolean notificationSendSetting = true; //default true
    private double thresholdProfit = 0; //default null
    private double thresholdLoss = 0; //default null
    double thresholdPrice = 0;
    private static double boughtPrice; // This attribute should fall under Orders or Portfolio?
    private double profitLossPerStock; // should = boughtPrice - currentPrice of Stock
    private List<Stock> stocks;
    private Map<Stock, List<Order>> buyOrders; // api de stock
    private Map<Stock, List<Order>> sellOrders;
    private Map<Stock, Integer> lotPool;
    static User user = new User();
    static Stock stock = new Stock();
    static Order order = new Order(user.getKey(), stock);
    static API api = new API();
    Database database = new Database();


    TradingEngine tradingEngine;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /*Notification(){
        this.thresholdPrice=thresholdPrice;
        this.updatedStockPrice= updatedStockPrice;

    }*/

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


    /*   public void start() {
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
   */
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
            message.setSubject("WallStreetWarrior");

            switch (caseSymbol) {
                case 1: //(updatedStockPrice > thresholdPrice)==true
                    message.setText("Your stock " + stock.getName() + " (" + stock.getSymbol() + ") has a profit of " + (thresholdPrice - updatedStockPrice));
                    break;

                case 2: //(updatedStockPrice < thresholdPrice)==true
                    message.setText("Your stock " + stock.getName() + " (" + stock.getSymbol() + ") has a loss of " + (updatedStockPrice - thresholdPrice));
                    break;

                case 3: //when buy order
                    message.setText("Your purchase order has went through successfully. ");
                    break;

                case 4: //when sell order
                    message.setText("Your stock has successfully been put up for sale. ");
                    break;

                case 5:
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

            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    /*    double boguhtPrice;
        final double price = boughtPrice;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double currentPrice = 0;
                String symbol = stock.getSymbol();

                //if (symbol != null){
                    currentPrice = 60 ;//api.getRealTimePrice(stock.getSymbol()) /* * quantity;
                //} else {
                //System.out.println("Symbol is null. Unable to fetch current price.");
                timer.cancel();
                return; // Exit the run() method if the symbol is null
            }
                if ( price - currentPrice > /*threshold 1){
                            sendNotification(2);
                        }
                        else if (currentPrice - price > /*threshold 1){
                            sendNotification(1);
                        }

                System.out.println(" ");

                //get the price of the stock in real time
                //get the price of the stock bought
                //compare the two prices
                //if the differences crosses the threshold then we send an email
                //
                System.out.println("Command executed");
            }
        }, 0, 1000); // 1000 milliseconds = 1 second
    }


        */
    }
}