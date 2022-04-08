package edu.gyu.mfa.db.bean;

public class InterCalProcessResult {
    private long id;
    private long model_id;
    private String name;
    private long timestamp;
    private String flux_value;
    private double parent_mean_norm2;
    private double cross_mean_norm2;
    private double mutation_mean_norm2;
    private double parent_min_norm2;
    private double cross_min_norm2;
    private double mutation_min_norm2;
    private String c_free_value;
    private int count;

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

    public String getFlux_value() {
        return flux_value;
    }

    public void setFlux_value(String flux_value) {
        this.flux_value = flux_value;
    }

    public double getParent_mean_norm2() {
        return parent_mean_norm2;
    }

    public void setParent_mean_norm2(double parent_mean_norm2) {
        this.parent_mean_norm2 = parent_mean_norm2;
    }

    public double getCross_mean_norm2() {
        return cross_mean_norm2;
    }

    public void setCross_mean_norm2(double cross_mean_norm2) {
        this.cross_mean_norm2 = cross_mean_norm2;
    }

    public double getMutation_mean_norm2() {
        return mutation_mean_norm2;
    }

    public void setMutation_mean_norm2(double mutation_mean_norm2) {
        this.mutation_mean_norm2 = mutation_mean_norm2;
    }

    public double getParent_min_norm2() {
        return parent_min_norm2;
    }

    public void setParent_min_norm2(double parent_min_norm2) {
        this.parent_min_norm2 = parent_min_norm2;
    }

    public double getCross_min_norm2() {
        return cross_min_norm2;
    }

    public void setCross_min_norm2(double cross_min_norm2) {
        this.cross_min_norm2 = cross_min_norm2;
    }

    public double getMutation_min_norm2() {
        return mutation_min_norm2;
    }

    public void setMutation_min_norm2(double mutation_min_norm2) {
        this.mutation_min_norm2 = mutation_min_norm2;
    }

    public String getC_free_value() {
        return c_free_value;
    }

    public void setC_free_value(String c_free_value) {
        this.c_free_value = c_free_value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
