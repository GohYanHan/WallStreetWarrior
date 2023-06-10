import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
getRealTimePrice() - For TradingEngine, it returns a double price ONLY
extractStocks() - return ArrayList of symbols and name
 */

public class API {
    public static String fileName = "MyStocks";
    public static final String API_KEY = "UM-1cd15cbc8ba9f613f94373ca35c267a52acf88978d73439e9f3c941b1c49318d";
    public static final String API_ENDPOINT = "https://wall-street-warriors-api-um.vercel.app/price";


    // Extract the stocks' symbols and names from the text file and store them in a List
    List<Stock> extractStocks() throws IOException {
        List<Stock> stockList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String symbol = parts[0];
                    String name = parts[1];

                    Stock stock = new Stock(symbol, name);
                    stockList.add(stock);
                }
            }
        }

        return stockList;
    }


    // Only return single value price for Trading Machine
    double getRealTimePrice(String symbol) throws IOException {

        String url = API_ENDPOINT + "?apikey=" + API_KEY + "&function=TIME_SERIES_INTRADAY_EXTENDED&symbol=" + symbol;
        URL apiURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        String jsonResponse = "";
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            jsonResponse = response.toString();
        }

        double latestClosePrice = 0;
        try {
            JSONObject json = new JSONObject(jsonResponse);

            if (!json.isNull(symbol)) {

                JSONObject symbolData = json.getJSONObject(symbol);
                JSONObject closeData = symbolData.getJSONObject("Close");

                long latestTimestamp = 0;

                Iterator<String> timestampIterator = closeData.keys();
                while (timestampIterator.hasNext()) {
                    String timestamp = timestampIterator.next();
                    long currentTimestamp = Long.parseLong(timestamp);
                    double close = closeData.getDouble(timestamp);

                    if (currentTimestamp > latestTimestamp) {
                        latestTimestamp = currentTimestamp;
                        latestClosePrice = close;
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return latestClosePrice;
    }


}