package cn.gyu.flux.db.bean;

public class LabelInputBean {

    private String name;
    private String code;
    private String value;

    public LabelInputBean(){}

    public LabelInputBean(String name, String code, String value) {
        this.name = name;
        this.code = code;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
