import java.util.*;

class Stock {
    private String name;
    private String ticker;

    public Stock(String name, String ticker) {
        this.name = name;
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }
}

class StockSearch {
    private List<Stock> stocks;

    public StockSearch() {
        stocks = new ArrayList<>();
    }

    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    public List<Stock> searchByName(String query) {
        List<Stock> results = new ArrayList<>();

        for (Stock stock : stocks) {
            if (boyerMooreSearch(stock.getName(), query)) {
                results.add(stock);
            }
        }

        return results;
    }

    public List<Stock> searchByTicker(String query) {
        List<Stock> results = new ArrayList<>();

        for (Stock stock : stocks) {
            if (boyerMooreSearch(stock.getTicker(), query)) {
                results.add(stock);
            }
        }

        return results;
    }

    private boolean boyerMooreSearch(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();

        int[] badChar = new int[256];
        Arrays.fill(badChar, -1);

        for (int i = 0; i < m; i++) {
            badChar[pattern.charAt(i)] = i;
        }

        int shift = 0;
        while (shift <= n - m) {
            int j = m - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) {
                return true;
            } else {
                shift += Math.max(1, j - badChar[text.charAt(shift + j)]);
            }
        }

        return false;
    }
}

public class StockSearchApp {
    public static void main(String[] args) {
        StockSearch stockSearch = new StockSearch();
        API api = new API();

        // Add some sample stocks
        stockSearch.addStock(new Stock(api.StockList()));


        // Search by name

        Scanner	k = new Scanner(System.in);


        String searchName = k.nextLine();;
        List<Stock> nameResults = stockSearch.searchByName(searchName);
        if (nameResults.isEmpty()) {
            System.out.println("No stocks found with name containing '" + searchName + "'");
        } else {
            System.out.println("Stocks with name containing '" + searchName + "':");
            for (Stock stock : nameResults) {
                System.out.println(stock.getName() + " (" + stock.getTicker() + ")");
            }
        }

        System.out.println();

        // Search by ticker
        String searchTicker = "";
        List<Stock> tickerResults = stockSearch.searchByTicker(searchTicker);
        if (tickerResults.isEmpty()) {
            System.out.println("No stocks found with ticker containing '" + searchTicker + "'");
        } else {
            System.out.println("Stocks with ticker containing '" + searchTicker + "':");
            for (Stock stock : tickerResults) {
                System.out.println(stock.getName() + " (" + stock.get
