package edu.gyu.mfa.isotopomer;

public abstract class AExIsotopomer {
    protected String name;
    protected String cPosition;
    protected double fraction;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getcPosition() {
        return cPosition;
    }

    public void setcPosition(String cPosition) {
        this.cPosition = cPosition;
    }

    public double getFraction() {
        return fraction;
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }
}
