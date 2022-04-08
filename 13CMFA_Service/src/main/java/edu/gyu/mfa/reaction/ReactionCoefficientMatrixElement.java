package edu.gyu.mfa.reaction;

import edu.gyu.mfa.matrix.CoefficientMatrixElement;
import edu.gyu.mfa.matrix.EMUCompositionCoefficientMatrixElement;

import java.util.ArrayList;
import java.util.List;

public class ReactionCoefficientMatrixElement {

    private List<CoefficientMatrixElement> singleInterReactionEMUElementList;
    private EMUCompositionCoefficientMatrixElement emuCompositionCoefficientMatrixElement;
    private List<CoefficientMatrixElement> consumptionReactionEMUElementList;
    private List<CoefficientMatrixElement> inputReactionEMUElementList;

    public ReactionCoefficientMatrixElement() {
        singleInterReactionEMUElementList = new ArrayList<>();
        consumptionReactionEMUElementList = new ArrayList<>();
        inputReactionEMUElementList = new ArrayList<>();
    }

    public void addInputReactionEMUElement(Flux flux, int mBase, int size, int tBase, String rName) {
        CoefficientMatrixElement element = new CoefficientMatrixElement(flux, mBase, size, tBase);
        element.setrName(rName);
        inputReactionEMUElementList.add(element);
    }

    public void addSingleInterReactionEMUElement(Flux flux, int mBase, int size, int tBase, String rName) {
        CoefficientMatrixElement element = new CoefficientMatrixElement(flux, mBase, size, tBase);
        element.setrName(rName);
        singleInterReactionEMUElementList.add(element);
    }

    public EMUCompositionCoefficientMatrixElement getEmuCompositionCoefficientMatrixElement() {
        return emuCompositionCoefficientMatrixElement;
    }

    public void setEmuCompositionCoefficientMatrixElement(EMUCompositionCoefficientMatrixElement emuCompositionCoefficientMatrixElement) {
        this.emuCompositionCoefficientMatrixElement = emuCompositionCoefficientMatrixElement;
    }

    public void addConsumptionReactionEMUElement(Flux flux, int mBase, int size, int tBase, String rName) {
        CoefficientMatrixElement element = new CoefficientMatrixElement(flux, mBase, size, tBase);
        element.setrName(rName);
        consumptionReactionEMUElementList.add(element);
    }

    public List<CoefficientMatrixElement> getSingleInterReactionEMUElementList() {
        return singleInterReactionEMUElementList;
    }

    public List<CoefficientMatrixElement> getConsumptionReactionEMUElementList() {
        return consumptionReactionEMUElementList;
    }

    public List<CoefficientMatrixElement> getInputReactionEMUElementList() {
        return inputReactionEMUElementList;
    }

}
