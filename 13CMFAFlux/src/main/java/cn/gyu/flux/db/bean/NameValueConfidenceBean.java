package cn.gyu.flux.db.bean;

public class NameValueConfidenceBean {

    private String name;
    private String value;
    private String confidence;

    public NameValueConfidenceBean(){}

    public NameValueConfidenceBean(String name, String value, String confidence) {
        this.name = name;
        this.value = value;
        this.confidence = confidence;
    }

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

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
