package cn.gyu.flux.db.bean;

public class CarbonNameComp {

    private String carbon;
    private String name;
    private boolean isReactant;

    public CarbonNameComp() {}

    public CarbonNameComp(String carbon, String name, boolean isReactant) {
        this.carbon = carbon;
        this.name = name;
        this.isReactant = isReactant;
    }

    public String getCarbon() {
        return carbon;
    }

    public void setCarbon(String carbon) {
        this.carbon = carbon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReactant() {
        return isReactant;
    }

    public void setReactant(boolean reactant) {
        isReactant = reactant;
    }
}
