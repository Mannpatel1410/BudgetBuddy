package factory;

import model.account.*;

public class AccountFactory {
    public static Account createAccount(String type, long userId, String name) {
        Account account;
        switch (type) {
            case "CHECKING":
                account = new CheckingAccount();
                break;
            case "SAVINGS":
                account = new SavingsAccount();
                break;
            case "CREDIT_CARD":
                account = new CreditCardAccount();
                break;
            default:
                throw new IllegalArgumentException("Unknown account type: " + type);
        }
        account.setUserId(userId);
        account.setAccountName(name);
        return account;
    }
}
