package dojo.liftpasspricing;

public class RateCalculator {
    private Rates rates;

    public RateCalculator() {
        rates = new Rates();
    }

    public float getBaseRate() {
        String baseRateJson = rates.getBaseRateJSON();
        float baseRate = parseBaseRateFromJson(baseRateJson);
        return baseRate;
    }

    private float parseBaseRateFromJson(String baseRateJson) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(baseRateJson).getAsJsonObject();
        float cost = jsonObject.get("cost").getAsFloat();
        return cost;
    }
}
