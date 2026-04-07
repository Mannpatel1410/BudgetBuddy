package adapter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;

public class CurrencyConversionAdapter implements CurrencyConverter {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    @Override
    public double convert(double amount, String fromCurrency, String toCurrency) {
        try {
            URL url = new URL(API_URL + fromCurrency);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            JsonObject response = JsonParser.parseReader(
                new InputStreamReader(connection.getInputStream())
            ).getAsJsonObject();

            JsonObject rates = response.getAsJsonObject("rates");
            if (rates == null || !rates.has(toCurrency)) {
                throw new IllegalArgumentException("Invalid currency: " + toCurrency);
            }

            double rate = rates.get(toCurrency).getAsDouble();
            return amount * rate;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Currency conversion failed: " + e.getMessage(), e);
        }
    }
}
