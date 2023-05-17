import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class API {
    public static void main(String[] args) {
        String fileName = "MyStocks";

        try {
            String jsonResponse = readJsonFromFile(fileName);
            displayStocks(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
