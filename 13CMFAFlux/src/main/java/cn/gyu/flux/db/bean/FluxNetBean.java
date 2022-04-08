package cn.gyu.flux.db.bean;

public class FluxNetBean {

    private String name;
    private String value;

    public FluxNetBean(){}

    public FluxNetBean(String name, String value) {
        this.name = name;
        this.value = value;
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
}
