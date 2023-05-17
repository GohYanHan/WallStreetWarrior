import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;


public class API {
    static String fileName = "MyStocks";
    private static final String API_KEY = "UM-1cd15cbc8ba9f613f94373ca35c267a52acf88978d73439e9f3c941b1c49318d";
    private static final String API_ENDPOINT = "https://wall-street-warriors-api-um.vercel.app/price";

    public static void main(String[] args) {
        try {
            String jsonResponse = readJsonFromFile(fileName);
            displayStocks(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getPrices();
    }

    static String readJsonFromFile(String fileName) throws IOException {
        StringBuilder jsonText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
        }

        return jsonText.toString();
    }

    static void displayStocks(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            System.out.printf("%-12s\t%-40s\n", "Symbol", "Name");
            System.out.println("----------------------------------------");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stockJson = jsonArray.getJSONObject(i);
                String symbol = stockJson.getString("symbol");
                String name = stockJson.getString("name");

                System.out.printf("%-12s\t%-40s\n", symbol, name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void getPrices() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter the stock symbol(s) separated by commas: ");
            String symbols = reader.readLine();
            String[] symbolsArr = symbols.split(",");

            System.out.print("Enter the desired timestamp (YYYY-MM-DD): ");
            String timestamp = reader.readLine();

            System.out.print("Enter the desired interval (1min, 5min, 15min, 30min, 60min, daily, weekly, monthly): ");
            String interval = reader.readLine();

            String jsonResponse = getStockPrice(symbolsArr, timestamp, interval);
            displayPrices(jsonResponse, symbolsArr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getStockPrice(String[] symbols, String timestamp, String interval) throws Exception {
        StringBuilder symbolsParam = new StringBuilder();
        for (String symbol : symbols) {
            symbolsParam.append(symbol).append(",");
        }
        symbolsParam.deleteCharAt(symbolsParam.length() - 1);

        String url = API_ENDPOINT + "?apikey=" + API_KEY + "&function=TIME_SERIES_INTRADAY_EXTENDED&symbol=" + symbolsParam.toString() + "&interval=" + interval + "&slice=" + timestamp;
        URL apiURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } else {
            throw new Exception("Failed to get stock price. Response code: " + responseCode);
        }
    }

    static void displayPrices(String jsonResponse, String[] symbols) {
        try {
            JSONObject json = new JSONObject(jsonResponse);

            for (String symbol : symbols) {
                JSONObject symbolData = json.getJSONObject(symbol);
                System.out.println("Symbol: " + symbol);
                System.out.println("-------------------------------");
                System.out.printf("%-20s %-20s %-20s %-20s %-20s\n", "Timestamp", "Open", "High", "Low", "Close");

                JSONObject openData = symbolData.getJSONObject("Open");
                JSONObject highData = symbolData.getJSONObject("High");
                JSONObject lowData = symbolData.getJSONObject("Low");
                JSONObject closeData = symbolData.getJSONObject("Close");

                Iterator<String> timestampIterator = openData.keys();
                while (timestampIterator.hasNext()) {
                    String timestamp = timestampIterator.next();
                    double open = openData.getDouble(timestamp);
                    double high = highData.getDouble(timestamp);
                    double low = lowData.getDouble(timestamp);
                    double close = closeData.getDouble(timestamp);

                    System.out.printf("%-20s %-20.2f %-20.2f %-20.2f %-20.2f\n", timestamp, open, high, low, close);
                }
                System.out.println();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
