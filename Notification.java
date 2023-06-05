import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class Notification {
    private double thresholdPrice = 0; //default null
    private double updatedStockPrice;
    private boolean notificationSendSetting = true; //default true

    private Map<Double, List> thresholdMap = new HashMap<>();
    User user;
    Stock stock;
    Order order;
    API api = new API();
    TradingEngine tradingEngine;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    //hashmap with 3 keys, name(PK)(from MyStocks iterated into a linkedlist), threshholdPrice(paired with the name), currentPrice/updatedStockPrice(paired with name)


    //on button click do set true or set false, default true, assuming there are 2 buttons(enable/disable)


    public void setNotificationSendSettingTrue() {
        this.notificationSendSetting = true;

        System.out.println("Notification setting has been turned on.");
    }

    public void setNotificationSendSettingFalse() {
        this.notificationSendSetting = false;
        System.out.println("Notification setting has been turned off.");
    }

    public void setThreshold() {
        for (int i = 0; i < tradingEngine.getBuyOrders().size(); i++) {
            thresholdMap.put(this.thresholdPrice, tradingEngine.getBuyOrders().get(i));
            System.out.println("Threshold for " + stock.getName() + " has been set to" + thresholdPrice);
        }
    }


    public void start() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Method to be called every 1 second
                try {
                    updateStockPrice();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Schedule the task to run every 1 second
        timer.schedule(task, 0, 1000);
    }

    public void updateStockPrice() throws IOException {
        updatedStockPrice = api.getRealTimePrice(order.getStock().getSymbol());
        this.updatedStockPrice = updatedStockPrice;
        System.out.println("Stock price updated: " + updatedStockPrice);
        if (updatedStockPrice > thresholdPrice) {
            sendNotif(1);
        } else if (updatedStockPrice < thresholdPrice) {
            sendNotif(2);
        }
    }
    //handled by timer, every XX call this method

    public void sendNotif(int caseSymbol) {
        String hostAddress = "smtp.example.com"; // SMTP server host
        String serverPort = "587"; // SMTP server port
        final String hostEmail = "your_email@example.com"; // Your email address
        final String hostPassword = "your_password"; // Your email password

        String toAddress = user.getEmail(); // Recipient's email address
        String subject = "WALL STREET WARRIORS"; // Email subject
        String body = ""; // Email body

        if (notificationSendSetting = true) {
            //check stock price if neg
            if (updatedStockPrice >= 0) {
                //iterate through hashmap, find if thresholdPrice more or less than updatedStockPrice based on PK name, then if true send email
                /*for (i = 0; i < hashmap.length(); i++){
                        iterate though the treshholdVal and updatedStockPrice, check if more or less, then take note of the index
                        when get index, get the names linkedlist from hashmap, then find the index
                        the return value is your stockName, then just send email
                */
                switch (caseSymbol) {

                    case 1: //(updatedStockPrice > thresholdPrice)==true
                        body = "Your stock " + stock.getName() + " has a profit of " + (thresholdPrice - updatedStockPrice);

                    case 2: //(updatedStockPrice < thresholdPrice)==true
                        body = "Your stock " + stock.getName() + " has a loss of " + (updatedStockPrice - thresholdPrice);

                    case 3: //when buy order successfully
                        body = "Thank you for buying " + stock.getName();

                    case 4: //when sell order
                        body = "Stock " + stock.getName() + "has been sold";
                }
            }
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", hostAddress);
        props.put("mail.smtp.port", serverPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(hostEmail, hostPassword);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(hostEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            System.out.println("Failed to send email. Error: " + e.getMessage());
        }
    }

    public void sendNotificationEnter() { //when buy
        if (notificationSendSetting = true) {
            //send email
            while (thresholdPrice != updatedStockPrice) {
                updateStockPrice(stock);
            }
        }
    }

    public void sendNotificationExit() { //when sell
        if (notificationSendSetting = true) {
            //send email
        }
    }
}

/*} else if (choice == 4) {
                System.out.println("1. Notification ON \n2.Notification OFF");
                choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) {
                    notification.setNotificationSendSettingTrue();
                } else if (choice == 2) {
                    notification.setNotificationSendSettingFalse();
                } else {
                    System.out.println("Execution invalid");
                    return;

    this is for notification setting in main menu



    private Notification notification;
    notification.sendNotif(3);
    notification.sendNotif(4);

    this is for notification sent after buy/sell
 */