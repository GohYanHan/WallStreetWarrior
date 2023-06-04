import org.json.JSONArray;
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
StockList() - Display a list of 'symbol' & 'name'
getPrices() - For user to search for prices (can search multiple stock at one time)
getRealTimePrice() - For TradingEngine, it returns a double price ONLY
extractStocks() - return ArrayList of symbols and name
 */

class testAPI {
    public static void main(String[] args) throws IOException {
        API api = new API();
//        api.StockList();
        api.getPrices();
//        System.out.println(api.getRealTimePrice("8206.MY"));
//        api.extractStocks();

    }
}

public class API {
    public static String fileName = "MyStocks";
    public static final String API_KEY = "UM-1cd15cbc8ba9f613f94373ca35c267a52acf88978d73439e9f3c941b1c49318d";
    public static final String API_ENDPOINT = "https://wall-street-warriors-api-um.vercel.app/price";

    private BoyerMoore boyerMoore;

    public API(){
        boyerMoore = new BoyerMoore();
    }

    //call this method to display a list of Malaysia Stock
    static void StockList() {
        try {
            String jsonResponse = readJsonFromFile(fileName);
            displayStocks(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Call this method to read JSON data from a file copied from API end point provided
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


    // Extract the stocks' symbols and names from the JSON response and store them in a List
    static List<Stock> extractStocks() throws IOException {
        String jsonResponse = readJsonFromFile(fileName);
        List<Stock> stockList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stockJson = jsonArray.getJSONObject(i);
                String symbol = stockJson.getString("symbol");
                String name = stockJson.getString("name");

                Stock stock = new Stock(symbol, name);
                stockList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stockList;
    }

    // Display the stocks' symbols & name from the JSON response
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
            System.out.println();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Only return single value price for Trading Machine
    static double getRealTimePrice(String symbol) throws IOException {
        symbol = symbol.replace(".MY", ".KL");

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


    // Prompt the user for stock symbols, timestamp, and interval, and display the prices by calling getStockPrice() and displayPrices()
    static void getPrices() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("***For multiple stocks, kindly request to separate them by commas [e.g. 0001.MY,0002.MY]***");
            System.out.print("Enter the stock symbol(s): ");
            String symbols = reader.readLine().trim();
            String[] symbolsArr = symbols.split("\\s*,\\s*");

            while (!isValidSymbols(symbolsArr)) {
                System.out.println("Stock symbol not found. Please enter another symbol.");
                System.out.println("***For multiple stocks, kindly request to separate them by commas [e.g. 0001.MY,0002.MY]***");
                System.out.print("Enter the stock symbol(s): ");
                symbols = reader.readLine().trim();
                symbolsArr = symbols.split("\\s*,\\s*");
            }


            // Retrieve the stock prices within the specified date range and interval
            String jsonResponse = getStockPrice(symbolsArr);
            displayPrices(jsonResponse, symbolsArr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Check if all symbols are in the stock list
    static boolean isValidSymbols(String[] symbols) {
        try {
            JSONArray jsonArray = new JSONArray(readJsonFromFile(fileName));

            for (String symbol : symbols) {
                boolean isValid = false;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject stockJson = jsonArray.getJSONObject(i);
                    String stockSymbol = stockJson.getString("symbol");

                    if (stockSymbol.equals(symbol)) {
                        isValid = true;
                        break;
                    }
                }

                if (!isValid) {
                    System.out.println("Invalid stock symbol: " + symbol);
                    return false;
                }
            }

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    // Fetch stock prices from the API based on the symbols, timestamp, and interval
    static String getStockPrice(String[] symbolsArr) throws Exception {

        // Convert the symbols to "0001.KL", "0002.KL", etc.
        for (int i = 0; i < symbolsArr.length; i++) {
            symbolsArr[i] = symbolsArr[i].replace(".MY", ".KL");
        }

        StringBuilder symbolsParam = new StringBuilder();
        for (String symbol : symbolsArr) {
            symbolsParam.append(symbol).append(",");
        }
        symbolsParam.deleteCharAt(symbolsParam.length() - 1);

        String url = API_ENDPOINT + "?apikey=" + API_KEY + "&function=TIME_SERIES_INTRADAY_EXTENDED&symbol=" + symbolsParam;
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


    // Display the timestamps and prices for the requested stocks
    static void displayPrices(String jsonResponse, String[] symbols) {
        try {
            JSONObject json = new JSONObject(jsonResponse);

            for (String symbol : symbols) {
                if (json.isNull(symbol)) {
                    System.out.println("No data available for symbol: " + symbol);
                    continue; // Skip to the next iteration if symbol is not found
                }

                System.out.println();
                JSONObject symbolData = json.getJSONObject(symbol);
                System.out.println("Symbol: " + symbol);

                JSONObject closeData = symbolData.getJSONObject("Close");

                long latestTimestamp = 0;
                double latestClosePrice = 0.0;

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
                System.out.println("Close: " + latestClosePrice);
                System.out.println();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


     void SearchdisplayStocks(String jsonResponse, String searchQuery) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            System.out.printf("%-12s\t%-40s\n", "Symbol", "Name");
            System.out.println("----------------------------------------");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stockJson = jsonArray.getJSONObject(i);
                String symbol = stockJson.getString("symbol");
                String name = stockJson.getString("name");

                // Use Boyer-Moore for string matching
                char[] text = symbol.toCharArray();
                char[] pattern = searchQuery.toCharArray();
                int index = boyerMoore.search(text, pattern);

                if (index != -1) {
                    System.out.printf("%-12s\t%-40s\n", symbol, name);
                }
            }
            System.out.println();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}