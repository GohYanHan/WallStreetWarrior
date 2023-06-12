import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class search {
    private static String fileName = "MyStocks";
    private static final String API_KEY = "UM-1cd15cbc8ba9f613f94373ca35c267a52acf88978d73439e9f3c941b1c49318d";
    private static final String API_ENDPOINT = "https://wall-street-warriors-api-um.vercel.app/price";


    private static BoyerMoore boyerMoore;

    private static API api;


    public search() {
        boyerMoore = new BoyerMoore();
        api = new API();
    }

    public static void main(String[] args) throws IOException {
        API api = new API();
        Scanner k = new Scanner(System.in);


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
            // Read the txt file
            List<String> lines = Files.readAllLines(Paths.get(fileName));

            for (String line : lines) {
                // Split the line into symbol and name
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String symbol = parts[0].trim();
                    String name = parts[1].trim();

                    // Search by symbol or name
                    if (symbol.toLowerCase().contains(query.toLowerCase()) || name.toLowerCase().contains(query.toLowerCase())) {
                        matchingStocks.add(new Stock(symbol, name));
                        found = true; // Match found
                    }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
