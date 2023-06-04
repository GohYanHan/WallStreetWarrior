public class Stock {
    private String symbol;
    private String name;
    private double price;

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public Stock(String symbol) {
        this.symbol = symbol;

    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}