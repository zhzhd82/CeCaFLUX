package cn.gyu.flux.db.bean;

public class OptimizingResultBean {

    private long id;
    private long model_id;
    private String name;
    private long timestamp;
    private String optimizing_flux;
    private double norm2;
    private String c_free_value;
    private String flux_confidence_interval;
    private String c_confidence_interval;
    private double goodness_of_fit;
    private String x_value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getModel_id() {
        return model_id;
    }

    public void setModel_id(long model_id) {
        this.model_id = model_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOptimizing_flux() {
        return optimizing_flux;
    }

    public void setOptimizing_flux(String optimizing_flux) {
        this.optimizing_flux = optimizing_flux;
    }

    public double getNorm2() {
        return norm2;
    }

    public void setNorm2(double norm2) {
        this.norm2 = norm2;
    }

    public String getC_free_value() {
        return c_free_value;
    }

    public void setC_free_value(String c_free_value) {
        this.c_free_value = c_free_value;
    }

    public String getFlux_confidence_interval() {
        return flux_confidence_interval;
    }

    public void setFlux_confidence_interval(String flux_confidence_interval) {
        this.flux_confidence_interval = flux_confidence_interval;
    }

    public String getC_confidence_interval() {
        return c_confidence_interval;
    }

    public void setC_confidence_interval(String c_confidence_interval) {
        this.c_confidence_interval = c_confidence_interval;
    }

    public double getGoodness_of_fit() {
        return goodness_of_fit;
    }

    public void setGoodness_of_fit(double goodness_of_fit) {
        this.goodness_of_fit = goodness_of_fit;
    }

    public String getX_value() {
        return x_value;
    }

    public void setX_value(String x_value) {
        this.x_value = x_value;
    }
}
