package edu.gyu.mfa.optimizer;

public class FAndS {

    private double[] F;
    private double[][] S;

    public FAndS(){}

    public FAndS(double[] F, double[][] S) {
        this.F = F;
        this.S = S;
    }

    public double[] getF() {
        return F;
    }

    public void setF(double[] f) {
        F = f;
    }

    public double[][] getS() {
        return S;
    }

    public void setS(double[][] s) {
        S = s;
    }
}
