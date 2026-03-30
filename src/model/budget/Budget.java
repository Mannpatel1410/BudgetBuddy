package model.budget;

import state.BudgetState;
import state.UnderLimitState;
import state.ApproachingLimitState;
import state.ExceededState;
import observer.BudgetObserver;
import model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class Budget implements Cloneable {
    private long id;
    private long userId;
    private long categoryId;
    private double limitAmount;
    private double spentAmount;
    private String month;
    private int year;
    private BudgetState currentState;
    private List<BudgetObserver> observers;

    public Budget() {
        this.currentState = new UnderLimitState();
        this.observers = new ArrayList<>();
        this.spentAmount = 0.0;
    }

    public Budget(long userId, long categoryId, double limitAmount, String month, int year) {
        this();
        this.userId = userId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
    }

    // Observer: register observer
    public void addObserver(BudgetObserver observer) {
        observers.add(observer);
    }

    // Observer: remove observer
    public void removeObserver(BudgetObserver observer) {
        observers.remove(observer);
    }

    // Observer: notify all registered observers
    public void notifyObservers(Transaction transaction) {
        for (BudgetObserver observer : observers) {
            observer.update(this, transaction);
        }
    }

    // State: process spending and transition state
    public void addSpending(double amount, Transaction transaction) {
        currentState.handleTransaction(this, amount);
        this.spentAmount += amount;
        updateState();
        notifyObservers(transaction);
    }

    // State: determine correct state based on spending percentage
    private void updateState() {
        double percentage = getPercentageUsed();
        if (percentage >= 100) {
            this.currentState = new ExceededState();
        } else if (percentage >= 80) {
            this.currentState = new ApproachingLimitState();
        } else {
            this.currentState = new UnderLimitState();
        }
    }

    public String getStatus() {
        return currentState.getStateName();
    }

    public String getStatusColor() {
        return currentState.getStatusColor();
    }

    public String getAlertMessage() {
        return currentState.getAlertMessage();
    }

    public double getRemaining() {
        return limitAmount - spentAmount;
    }

    public double getPercentageUsed() {
        if (limitAmount == 0) return 0;
        return (spentAmount / limitAmount) * 100;
    }

    // Prototype: clone for next month (Milestone 3 full impl)
    @Override
    public Budget clone() {
        try {
            Budget cloned = (Budget) super.clone();
            cloned.id = 0;
            cloned.spentAmount = 0.0;
            cloned.currentState = new UnderLimitState();
            cloned.observers = new ArrayList<>();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BudgetState getCurrentState() { return currentState; }
    public void setCurrentState(BudgetState state) { this.currentState = state; }
}
