package edu.gyu.mfa.matrix;

import edu.gyu.mfa.reaction.Flux;
import java.util.ArrayList;
import java.util.List;

public class EMUCompositionCoefficientMatrixElement {

    private List<CoefficientMatrixElement> emu1CoefficientMatrixElementList;
    private List<CoefficientMatrixElement> emu2CoefficientMatrixElementList;

    public EMUCompositionCoefficientMatrixElement() {
        emu1CoefficientMatrixElementList = new ArrayList<>();
        emu2CoefficientMatrixElementList = new ArrayList<>();
    }

    public void addEMU1Element(Flux flux, int mBase, int size, int tBase, String rName) {
        CoefficientMatrixElement element = new CoefficientMatrixElement(flux, mBase, size, tBase);
        element.setrName(rName);
        emu1CoefficientMatrixElementList.add(element);
    }

    public void addEMU2Element(Flux flux, int mBase, int size, int tBase, String rName) {
        CoefficientMatrixElement element = new CoefficientMatrixElement(flux, mBase, size, tBase);
        element.setrName(rName);
        emu2CoefficientMatrixElementList.add(element);
    }

    public List<CoefficientMatrixElement> getEmu1CoefficientMatrixElementList() {
        return emu1CoefficientMatrixElementList;
    }
    public CoefficientMatrixElement getEmu1CoefficientMatrixElement(int index) {
        return emu1CoefficientMatrixElementList.get(index);
    }

    public List<CoefficientMatrixElement> getEmu2CoefficientMatrixElementList() {
        return emu2CoefficientMatrixElementList;
    }

    public CoefficientMatrixElement getEmu2CoefficientMatrixElement(int index) {
        return emu2CoefficientMatrixElementList.get(index);
    }

}
