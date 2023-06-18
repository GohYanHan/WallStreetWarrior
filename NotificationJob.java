import org.quartz.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class NotificationJob implements StatefulJob {
    private final Database db = new Database();
    private User user = db.getUser();
    private final Notification notification = new Notification();
    private final API api = new API();
    private boolean notificationSent = false;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //System.out.println("Timer is running...");
        double thresholds = user.getThresholds();
        //System.out.println(thresholds);
        if (thresholds == 0) {
            return;
        }

        List<Order> orders = db.loadTransactionHistory(user.getKey());

        Order currentOrder = orders.get(orders.size() - 1); // Get the latest order
        double boughtPrice = currentOrder.getExpectedBuyingPrice(); // Bought price
        double currentPrice;

        if (!getNotificationSent()) {
            //System.out.println("Bought Price: " + boughtPrice);
            try {
                currentPrice = api.getRealTimePrice(currentOrder.getStock().getSymbol()) * currentOrder.getShares();
            } catch (IOException e) {
                throw new JobExecutionException(e);
            }

            //DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            //String formattedCurrentPrice = decimalFormat.format(currentPrice);
            //System.out.println("Current Price: " + formattedCurrentPrice);

            if (currentPrice >= boughtPrice + thresholds) {
                notification.sendNotification(1, user.getEmail(), currentOrder);
                setNotificationSent(true); // Set the flag to true
            } else if (currentPrice <= boughtPrice - thresholds) {
                notification.sendNotification(2, user.getEmail(), currentOrder);
                setNotificationSent(true); // Set the flag to true
            }
        }

        if (getNotificationSent()) {
            //System.out.println("Email sent. Scheduler will stop for 1 hour.");

            // Stop the scheduler if a notification has been sent
            try {
                Scheduler scheduler = context.getScheduler();
                scheduler.pauseTrigger(context.getTrigger().getKey());

                // Schedule a new trigger to resume the job after 1 hour
                String stockSymbol = context.getJobDetail().getKey().getName().substring("notificationJob.".length());
                int counter = Integer.parseInt(stockSymbol.substring(stockSymbol.length() - 1));
                String triggerIdentifier = "notificationTrigger." + stockSymbol + "_" + counter;
                Trigger newTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerIdentifier, "group1")
                        .startAt(Date.from(Instant.now().plusSeconds(3600))) // Resume after 1 hour
                        .build();

                scheduler.scheduleJob(newTrigger);
                //System.out.println("Scheduler paused. Resuming in 1 hour.");
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }
        }

    }
    public boolean getNotificationSent() {
        return this.notificationSent;
    }
    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
}