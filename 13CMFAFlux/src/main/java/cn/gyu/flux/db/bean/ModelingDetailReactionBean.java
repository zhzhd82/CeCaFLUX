package cn.gyu.flux.db.bean;

public class ModelingDetailReactionBean {

    private String name;
    private String reactants;
    private String products;
    private double value;
    private String confidence_interval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReactants() {
        return reactants;
    }

    public void setReactants(String reactants) {
        this.reactants = reactants;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getConfidence_interval() {
        return confidence_interval;
    }

    public void setConfidence_interval(String confidence_interval) {
        this.confidence_interval = confidence_interval;
    }
}
