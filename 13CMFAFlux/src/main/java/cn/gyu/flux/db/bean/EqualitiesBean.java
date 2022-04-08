package cn.gyu.flux.db.bean;

public class EqualitiesBean {

    private String coefficient_reaction1;
    private String coefficient_reaction2;
    private String value;

    public EqualitiesBean(){}

    public EqualitiesBean(String coefficient_reaction1, String coefficient_reaction2, String value) {
        this.value = value;
        this.coefficient_reaction1 = coefficient_reaction1;
        this.coefficient_reaction2 = coefficient_reaction2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCoefficient_reaction1() {
        return coefficient_reaction1;
    }

    public void setCoefficient_reaction1(String coefficient_reaction1) {
        this.coefficient_reaction1 = coefficient_reaction1;
    }

    public String getCoefficient_reaction2() {
        return coefficient_reaction2;
    }

    public void setCoefficient_reaction2(String coefficient_reaction2) {
        this.coefficient_reaction2 = coefficient_reaction2;
    }
}
