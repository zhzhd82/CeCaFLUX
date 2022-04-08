package cn.gyu.flux.db.bean;

public class CFreeEstimatedBean {
    private String name;
    private String value;
    private String confidence_interval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getConfidence_interval() {
        return confidence_interval;
    }

    public void setConfidence_interval(String confidence_interval) {
        this.confidence_interval = confidence_interval;
    }
}
