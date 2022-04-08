package edu.gyu.mfa.ftbl;

public class MassSpectrometryTimePointValue {
    private String weightKey;
    private double value;
    private double deviation;
    private double timePoint;

    public MassSpectrometryTimePointValue(){}

    public MassSpectrometryTimePointValue(String weightKey, double value, double deviation, double timePoint) {
        this.weightKey = weightKey;
        this.value = value;
        this.deviation = deviation;
        this.timePoint = timePoint;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public double getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(double timePoint) {
        this.timePoint = timePoint;
    }

    public String getWeightKey() {
        return weightKey;
    }

    public void setWeightKey(String weightKey) {
        this.weightKey = weightKey;
    }
}
