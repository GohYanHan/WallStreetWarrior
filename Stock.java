public class Stock {
    private String symbol;
    private String name;

    public Stock() {
    }

    ;

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}