package edu.gyu.mfa.rungekutta;

import edu.gyu.mfa.reaction.ReactionCoefficientMatrixElement;

import java.util.Map;

public class BaseDerivn {

    private Map<String, Double> fluxPartialDifferentialMap;
    private double[] inputReactionInitX;
    private int interReactionXLength;
    private ReactionCoefficientMatrixElement reactionCoefficientMatrixElement;

    public void setFluxPartialDifferentialMap(Map<String, Double> fluxPartialDifferentialMap) {
        this.fluxPartialDifferentialMap = fluxPartialDifferentialMap;
    }

    public Map<String, Double> getFluxPartialDifferentialMap() {
        return fluxPartialDifferentialMap;
    }

    public double[] getInputReactionInitX() {
        return inputReactionInitX;
    }

    public void setInputReactionInitX(double[] inputReactionInitX) {
        this.inputReactionInitX = inputReactionInitX;
    }

    public int getInterReactionXLength() {
        return interReactionXLength;
    }

    public void setInterReactionXLength(int interReactionXLength) {
        this.interReactionXLength = interReactionXLength;
    }

    public ReactionCoefficientMatrixElement getReactionCoefficientMatrixElement() {
        return reactionCoefficientMatrixElement;
    }

    public void setReactionCoefficientMatrixElement(ReactionCoefficientMatrixElement reactionCoefficientMatrixElement) {
        this.reactionCoefficientMatrixElement = reactionCoefficientMatrixElement;
    }
}
