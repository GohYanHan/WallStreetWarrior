import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class search {
    private static String fileName = "MyStocks";
    private static BoyerMoore boyerMoore;
    private static API api;

    public search(){
        boyerMoore = new BoyerMoore();
        api = new API();
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
    void searchStocks(String[] queries) {
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
                    // Search by symbol or name for each query
                    if (symbol.toLowerCase().contains(queries[0].toLowerCase()) || name.toLowerCase().contains(queries[0].toLowerCase())) {
                        matchingStocks.add(new Stock(symbol, name));
                        found = true; // Match found
                    }
                    for (int q = 1; q < queries.length; q++) {
                        // Search by symbol or name using Boyer-Moore algorithm
                        if (boyerMoore.search(symbol.toLowerCase().toCharArray(), queries[q].toLowerCase().toCharArray()) != -1 || boyerMoore.search(name.toLowerCase().toCharArray(), queries[q].toLowerCase().toCharArray()) != -1) {
                            matchingStocks.add(new Stock(symbol, name));
                            found = true; // Match found
                        }
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
