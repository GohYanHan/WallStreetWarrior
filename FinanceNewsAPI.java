import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FinanceNewsAPI {
    private final String API_TOKEN = "ieWlFU3WOV1SJbvdxZDK4ssIKQPdkelX6HnWTnTd";

    public FinanceNewsAPI() {

    }

    public void getNews() throws IOException {
        URL url = new URL(String.format("https://api.marketaux.com/v1/news/all?country=my&language=en&limit=3&api_token=%s",
                API_TOKEN));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            String jsonResponse = response.toString();

            try {
                JSONObject responseObject = new JSONObject(jsonResponse);
                JSONObject metaObject = responseObject.getJSONObject("meta");
                int found = metaObject.getInt("found");
                int returned = metaObject.getInt("returned");
                JSONArray newsArray = responseObject.getJSONArray("data");

//                System.out.println("Found: " + found);
//                System.out.println("Returned: " + returned);

                int totalNews = newsArray.length();

                if (totalNews >= 3) {
                    List<JSONObject> randomNews = new ArrayList<>();
                    Random random = new Random();

                    // Get three random news articles
                    for (int i = 0; i < 3; i++) {
                        int randomIndex = random.nextInt(totalNews);
                        randomNews.add(newsArray.getJSONObject(randomIndex));
                    }

                    for (JSONObject newsObject : randomNews) {
                        String title = newsObject.getString("title");
                        String description = newsObject.getString("description");
                        String snippet = newsObject.getString("snippet");
                        String publishedAt = newsObject.getString("published_at");
                        String url_link = newsObject.getString("url");
                        JSONArray entitiesArray = newsObject.getJSONArray("entities");

                        System.out.println("***");
                        System.out.println("Title: " + title);
                        System.out.println("Description: " + description);
                        System.out.println("Snippet: " + snippet);
                        System.out.println("Published At: " + publishedAt);
                        System.out.println("Check it out at: " + url_link);
                        System.out.println();

                        for (int j = 0; j < entitiesArray.length(); j++) {
                            JSONObject entityObject = entitiesArray.getJSONObject(j);
                            String symbol = entityObject.getString("symbol");
                            String name = entityObject.getString("name");
                            String exchange = entityObject.getString("exchange");

                            System.out.println("Symbol: " + symbol);
                            System.out.println("Name: " + name);
                            System.out.println("Exchange: " + exchange);
                            System.out.println();
                        }
                    }
                } else {
                    System.out.println("Insufficient news articles found for the specified country.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: " + responseCode);
        }
    }
}
