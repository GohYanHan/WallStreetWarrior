import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

class search {
    private static String fileName = "MyStocks";
    private static final String API_KEY = "UM-1cd15cbc8ba9f613f94373ca35c267a52acf88978d73439e9f3c941b1c49318d";
    private static final String API_ENDPOINT = "https://wall-street-warriors-api-um.vercel.app/price";


    private static BoyerMoore boyerMoore;

    private static API api;


    public search(){
        boyerMoore = new BoyerMoore();
        api = new API();
    }

    public static void main(String[] args) throws IOException {
        API api = new API();
        Scanner k = new Scanner(System.in);


        api.searchDisplayStocks(readJsonFromFile(fileName),k.nextLine());
    }



    // Call this method to display a list of Malaysia Stock
    static void displayStocks() {
        try {
            String jsonResponse = readJsonFromFile(fileName);
            displayStocks(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Call this method to read JSON data from a file copied from API end point provided
    private static String readJsonFromFile(String fileName) throws IOException {
        StringBuilder jsonText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
        }

        return jsonText.toString();
    }

    // Display the stocks' symbols & name from the JSON response
    private static void displayStocks(String jsonResponse) {
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

    // Search for stocks by name or ticker symbol using Boyer-Moore algorithm
    static void searchStocks(String query) {
        boolean found = false; // Flag to track if a match is found
        List<Stock> matchingStocks = new ArrayList<>(); // List to store matching stocks

        try {
            String jsonResponse = readJsonFromFile(fileName);
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stockJson = jsonArray.getJSONObject(i);
                String symbol = stockJson.getString("symbol");
                String name = stockJson.getString("name");

                // Search by symbol
                if (symbol.toLowerCase().contains(query.toLowerCase())) {
                    matchingStocks.add(new Stock(symbol, name));
                    found = true; // Match found
                }
                // Search by name
                else if (boyerMoore.search(name.toLowerCase().toCharArray(), query.toLowerCase().toCharArray()) != -1) {
                    matchingStocks.add(new Stock(symbol, name));
                    found = true; // Match found
                }
            }

            if (found) {
                System.out.println("==================================================================================================");
                System.out.printf("|%-10s | %-50s | %-30s|\n", "Symbol", "Name", "Current Price per share (RM)");
                System.out.println("--------------------------------------------------------------------------------------------------");
                for (Stock stock : matchingStocks) {
                    System.out.printf("|%-10s | %-50s | %-30s|\n", stock.getSymbol(), stock.getName(), api.getRealTimePrice(stock.getSymbol()));
                }
                System.out.println("==================================================================================================");


            } else {
                System.out.println("Stock not found.");
            }
            System.out.println();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
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

            System.out.print("Enter the start date (yyyyMMdd): ");
            String startDateStr = reader.readLine();

            System.out.print("Enter the end date (yyyyMMdd): ");
            String endDateStr = reader.readLine();

            System.out.print("Enter the desired interval (1min, 5min, 15min, 30min, 60min, daily, weekly, monthly): ");
            String interval = reader.readLine();

            // Convert the start and end date strings to Date objects
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            // Convert the Date objects to timestamps
            long startTimestamp = startDate.getTime();
            long endTimestamp = endDate.getTime();

            // Retrieve the stock prices within the specified date range and interval
            String jsonResponse = getStockPrice(symbolsArr, startTimestamp, endTimestamp, interval);
            displayPrices(jsonResponse, symbolsArr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if all symbols are in the stock list
    private static boolean isValidSymbols(String[] symbols) {
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
    private static String getStockPrice(String[] symbolsArr, long startTimestamp, long endTimestamp, String interval) throws Exception {

        // Convert the symbols to "0001.KL", "0002.KL", etc.
        for (int i = 0; i < symbolsArr.length; i++) {
            symbolsArr[i] = symbolsArr[i].replace(".MY", ".KL");
        }

        StringBuilder symbolsParam = new StringBuilder();
        for (String symbol : symbolsArr) {
            symbolsParam.append(symbol).append(",");
        }
        symbolsParam.deleteCharAt(symbolsParam.length() - 1);

        String url = API_ENDPOINT + "?apikey=" + API_KEY + "&function=TIME_SERIES_INTRADAY_EXTENDED&symbol=" + symbolsParam + "&interval=" + interval + "&slice=" + startTimestamp + "," + endTimestamp;
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
    private static void displayPrices(String jsonResponse, String[] symbols) {
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
