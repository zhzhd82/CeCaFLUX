package edu.gyu.mfa.optimizer;

import edu.gyu.mfa.info.CAndCFree;
import edu.gyu.mfa.x.EMUX;

public class BoundInfo {

    private int index;
    private double bound;
    private double[] Ni;
    private boolean isUpperBound;
    private double norm2_opt;

    private EMUX emux;
    private CAndCFree cAndCFree;

    public BoundInfo(){}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getBound() {
        return bound;
    }

    public void setBound(double bound) {
        this.bound = bound;
    }

    public double[] getNi() {
        return Ni;
    }

    public void setNi(double[] ni) {
        Ni = ni;
    }

    public boolean isUpperBound() {
        return isUpperBound;
    }

    public void setUpperBound(boolean upperBound) {
        isUpperBound = upperBound;
    }

    public double getNorm2_opt() {
        return norm2_opt;
    }

    public void setNorm2_opt(double norm2_opt) {
        this.norm2_opt = norm2_opt;
    }

    public EMUX getEmux() {
        return emux;
    }

    public void setEmux(EMUX emux) {
        this.emux = emux;
    }

    public CAndCFree getcAndCFree() {
        return cAndCFree;
    }

    public void setcAndCFree(CAndCFree cAndCFree) {
        this.cAndCFree = cAndCFree;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("index : " + index + ", isUpperBound : " + isUpperBound);
        return sb.toString();
    }

}
