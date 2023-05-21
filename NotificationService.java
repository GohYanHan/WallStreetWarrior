import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// Create a Notification class
class Notification {
    private String userId;
    private double pnl;
    private boolean enteredPosition;
    private boolean exitedPosition;

    public Notification(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public double getPnl() {
        return pnl;
    }

    public void setPnl(double pnl) {
        this.pnl = pnl;
    }

    public boolean hasEnteredPosition() {
        return enteredPosition;
    }

    public void setEnteredPosition(boolean enteredPosition) {
        this.enteredPosition = enteredPosition;
    }

    public boolean hasExitedPosition() {
        return exitedPosition;
    }

    public void setExitedPosition(boolean exitedPosition) {
        this.exitedPosition = exitedPosition;
    }
}

// Utilize a third-party library like JavaMail for sending notifications
class EmailService {
    public void sendNotification(String userId, String message) {
        // Logic for sending email using JavaMail library
        System.out.println("Sending email notification to user " + userId + ": " + message);
    }
}

// Implement methods for handling user settings
class UserSettings {
    private double pnlThreshold;
    private boolean enableNotifications;

    public double getPnlThreshold() {
        return pnlThreshold;
    }

    public void setPnlThreshold(double pnlThreshold) {
        this.pnlThreshold = pnlThreshold;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }
}

// Use a timer or scheduling framework to periodically check for threshold crossing
class NotificationScheduler {
    private List<Notification> notifications;
    private EmailService emailService;
    private UserSettings userSettings;

    public NotificationScheduler() {
        this.notifications = new ArrayList<>();
        this.emailService = new EmailService();
        this.userSettings = new UserSettings();
    }

    public void startScheduler() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkThresholds();
            }
        }, 0, 1000); 
    }

    private void checkThresholds() {
        for (Notification notification : notifications) {
            if (notification.hasEnteredPosition() && notification.getPnl() > userSettings.getPnlThreshold()) {
                sendNotification(notification, "Your P&L crossed the threshold: " + notification.getPnl());
            } else if (notification.hasExitedPosition() && notification.getPnl() < userSettings.getPnlThreshold()) {
                sendNotification(notification, "Your P&L dropped below the threshold: " + notification.getPnl());
            }
        }
    }

    private void sendNotification(Notification notification, String message) {
        if (userSettings.isEnableNotifications()) {
            emailService.sendNotification(notification.getUserId(), message);
        }
    }

    // Additional methods for managing user settings and notifications
    public void addUserNotification(Notification notification) {
        notifications.add(notification);
    }

    public void removeUserNotification(Notification notification) {
        notifications.remove(notification);
    }

