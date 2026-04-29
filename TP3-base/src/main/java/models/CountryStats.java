public class CountryStats {

    private String country;
    private double value;

    public CountryStats() {
        // needed for JSON deserialization (Kafka Connect / Gson)
    }

    public CountryStats(String country, double value) {
        this.country = country;
        this.value = value;
    }

    public String getcountry() {
        return country;
    }

    public void setcountry(String country) {
        this.country = country;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}