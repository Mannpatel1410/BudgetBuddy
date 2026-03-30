package observer;

import model.budget.Budget;
import model.transaction.Transaction;
import model.Notification;
import dao.NotificationDAO;

public class AlertNotifier implements BudgetObserver {
    private NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void update(Budget budget, Transaction transaction) {
        double pct = budget.getPercentageUsed();

        if (pct >= 100) {
            Notification n = new Notification(
                budget.getUserId(),
                budget.getId(),
                "Budget EXCEEDED! You've spent $" + String.format("%.2f", budget.getSpentAmount())
                    + " of your $" + String.format("%.2f", budget.getLimitAmount()) + " limit.",
                "EXCEEDED"
            );
            notificationDAO.insert(n);
            System.out.println("ALERT: " + n.getMessage());

        } else if (pct >= 80) {
            Notification n = new Notification(
                budget.getUserId(),
                budget.getId(),
                "Budget WARNING: You've used " + String.format("%.1f", pct)
                    + "% of your budget. Only $" + String.format("%.2f", budget.getRemaining()) + " left.",
                "WARNING"
            );
            notificationDAO.insert(n);
            System.out.println("WARNING: " + n.getMessage());
        }
    }
}