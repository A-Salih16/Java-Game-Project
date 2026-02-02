package foodchain.model;

/**
 * Data structure holding the names of entities in a specific food chain.
 */
public class FoodChain {
    private final String apex;
    private final String pred;
    private final String prey;
    private final String food;

    public FoodChain(String apex,String pred,String prey,String food) {
        this.apex=apex;
        this.pred=pred;
        this.prey=prey;
        this.food=food;
    }
    public String getApexName() { return apex; }
    public String getPredatorName() { return pred; }
    public String getPreyName() { return prey; }
    public String getFoodName() { return food; }

    public String toString() {
        return apex+", "+pred+", "+prey+", "+food;
    }
}