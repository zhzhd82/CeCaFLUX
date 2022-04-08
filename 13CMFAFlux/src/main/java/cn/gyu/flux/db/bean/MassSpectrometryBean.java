package cn.gyu.flux.db.bean;

public class MassSpectrometryBean {

    private String name;
    private String fragment;
    private String mass;
    private String value;
    private String standard_deviation;
    private String time_point;

    public MassSpectrometryBean(){}

    public MassSpectrometryBean(String name, String fragment, String mass, String value,
                                String standard_deviation, String time_point) {
        this.name = name;
        this.fragment = fragment;
        this.mass = mass;
        this.value = value;
        this.standard_deviation = standard_deviation;
        this.time_point = time_point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStandard_deviation() {
        return standard_deviation;
    }

    public void setStandard_deviation(String standard_deviation) {
        this.standard_deviation = standard_deviation;
    }

    public String getTime_point() {
        return time_point;
    }

    public void setTime_point(String time_point) {
        this.time_point = time_point;
    }
}
