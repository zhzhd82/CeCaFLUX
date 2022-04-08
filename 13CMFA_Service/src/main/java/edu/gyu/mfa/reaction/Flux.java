package edu.gyu.mfa.reaction;

public class Flux implements Cloneable {

    private double value;
    private boolean consed;
    private int index;

    public Flux(double value) {
        this.value = value;
    }

    public Flux(int index) {
        this.index = index;
    }

    public Flux(boolean consed) {
        this.consed = consed;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isConsed() {
        return consed;
    }

    public void setConsed(boolean consed) {
        this.consed = consed;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Flux{" +
                "value=" + value +
                ", consed=" + consed +
                ", index=" + index +
                '}';
    }

    @Override
    public Flux clone() {
        Object obj;
        try {
            obj = super.clone();
            return (Flux) obj;
        } catch(CloneNotSupportedException e) {
            return null;
        }
    }
}
