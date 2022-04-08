package edu.gyu.mfa.info;

import edu.gyu.mfa.matrix.VectorTool;
import java.util.Arrays;

public class Norm2ValueFluxX implements Comparable<Norm2ValueFluxX> {

    private double norm2_value;
    private double[] fluxX;
    private double[] vFreeValues;
    private double[] cFreeValues;
    private double[] F;

    public Norm2ValueFluxX() {}

    public double getNorm2_value() {
        return norm2_value;
    }

    public void setNorm2_value(double norm2_value) {
        this.norm2_value = norm2_value;
    }

    public double[] getFluxX() {
        return fluxX;
    }

    public void setFluxX(double[] fluxX) {
        this.fluxX = fluxX;
        generateCAndVFreeValuesFromFluxX();
    }

    @Override
    public int compareTo(Norm2ValueFluxX o) {
        double diff = norm2_value - o.getNorm2_value();
        int result = 0;
        if(diff < 0) {
            result = -1;
        } else if(diff > 0) {
            result = 1;
        }
        return result;
    }

    @Override
    public String toString() {
        return "Norm2ValueFluxX{" +
                "norm2_value=" + norm2_value +
                ", fluxX=" + Arrays.toString(fluxX) +
                '}';
    }

    public double[] getvFreeValues() {
        return vFreeValues;
    }

    public void setvFreeValues(double[] vFreeValues) {
        this.vFreeValues = vFreeValues;
    }

    public double[] getcFreeValues() {
        return cFreeValues;
    }

    public void setcFreeValues(double[] cFreeValues) {
        this.cFreeValues = cFreeValues;
    }

    public void generateFluxXFromVAndCFreeValues() {
        fluxX = VectorTool.appendVector(cFreeValues, vFreeValues);
    }

    public void generateCAndVFreeValuesFromFluxX() {
        vFreeValues = VectorTool.getSubVetor(fluxX, 0, Count.freeCount);
        cFreeValues = VectorTool.getSubVetor(fluxX, Count.freeCount, fluxX.length);
    }

    public double[] getF() {
        return F;
    }

    public void setF(double[] f) {
        F = f;
    }
}
