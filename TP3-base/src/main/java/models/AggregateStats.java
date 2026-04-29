public class AggregateStats {

    private double totalRevenue;
    private double totalExpenses;
    private double totalProfit;

    public AggregateStats() {
        // needed for JSON deserialization (Kafka Connect / Gson)
    }

    public AggregateStats(double totalRevenue, double totalExpenses, double totalProfit) {
        this.totalRevenue = totalRevenue;
        this.totalExpenses = totalExpenses;
        this.totalProfit = totalProfit;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }
}