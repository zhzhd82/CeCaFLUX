package edu.gyu.mfa.matrix;

import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.reaction.Flux;

public class CoefficientMatrixElement {
    private Flux flux;
    private int mBase;
    private EMU mEMU;
    private int size;
    private int tBase;
    private EMU tEMU;
    private String rName;

    public CoefficientMatrixElement(Flux flux, int mBase, int size, int tBase) {
        this.flux = flux;
        this.mBase = mBase;
        this.size = size;
        this.tBase = tBase;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getmBase() {
        return mBase;
    }

    public int gettBase() {
        return tBase;
    }

    public double getFluxValue() {
        return flux.getValue();
    }

    public void setFluxValue(double value) {
        flux.setValue(value);
    }

    public EMU getmEMU() {
        return mEMU;
    }

    public void setmEMU(EMU mEMU) {
        this.mEMU = mEMU;
    }

    public EMU gettEMU() {
        return tEMU;
    }

    public void settEMU(EMU tEMU) {
        this.tEMU = tEMU;
    }

    public String getrName() {
        return rName;
    }

    public void setrName(String rName) {
        this.rName = rName;
    }

    public Flux getFlux() {
        return flux;
    }
}
