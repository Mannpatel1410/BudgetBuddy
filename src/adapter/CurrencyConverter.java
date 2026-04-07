package adapter;

public interface CurrencyConverter {
    double convert(double amount, String fromCurrency, String toCurrency);
}