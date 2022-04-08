package edu.gyu.mfa.x;

import edu.gyu.mfa.calculator.ReactionFluxCalculator;
import edu.gyu.mfa.calculator.SizeBaseCalculator;
import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.matrix.CoefficientMatrixElement;
import edu.gyu.mfa.matrix.EMUCompositionCoefficientMatrixElement;
import edu.gyu.mfa.reaction.*;
import edu.gyu.mfa.rungekutta.BaseDerivn;
import edu.gyu.mfa.rungekutta.FlanaganDerivn;
import edu.gyu.mfa.rungekutta.RungeKuttaParameter;
import flanagan.integration.DerivnFunction;
import flanagan.integration.RungeKutta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EMUX extends AbsReactionSizeEMU {

    private BaseDerivn baseDerivn;
    private double[] interReactionInitX;
    private double[] inputReactionInitX;
    private SizeBaseCalculator sbCalculator;

    public EMUX(ReactionSizeEMU reactionSizeEMU,
                List<EMUReaction> emuReactions, ReactionFluxCalculator reactionFluxCalculator) {
        super(reactionSizeEMU);
        sbCalculator = new SizeBaseCalculator(interReactionSizeEMUsMap, inputReactionSizeEMUsMap);
        init(emuReactions, reactionFluxCalculator.calculatePartialDifferential());
    }

    public double[] calculateX(Map<Integer, Double> C_PosValueMap, Map<Integer, Double> CLCJ_PosValueMap, double T0, double Tn, double[] X0) {
        List<Double> hTimePointList = new ArrayList<>();
        FlanaganDerivn flanaganDerivn = new FlanaganDerivn(baseDerivn);
        flanaganDerivn.setInterReactionSizeEMUsMap(interReactionSizeEMUsMap);
        flanaganDerivn.sethTimePointList(hTimePointList);
        flanaganDerivn.setC_PosValueMap(C_PosValueMap);
        boolean isCalculatingDxdw = false;
        if(CLCJ_PosValueMap != null) {
            isCalculatingDxdw = true;
            flanaganDerivn.setCLCJ_PosValueMap(CLCJ_PosValueMap);
        }
        flanaganDerivn.setCalculatingDxdw(isCalculatingDxdw);
        if(X0 == null) {
            if(isCalculatingDxdw) {
                X0 = XTool.concatenateX(interReactionInitX, Count.freeCount + Count.cCount);
            } else {
                X0 = interReactionInitX;
            }
        }
        double Xn[] = null;
        try {
            if(isCalculatingDxdw) {
                Xn = rungeKuttaFixedFour(RungeKuttaParameter.Step, T0, Tn, X0, flanaganDerivn);
            } else if(Argument.isAdaptive) {
                Xn = rungeKuttaAdaptiveFour(RungeKuttaParameter.Step, T0, Tn, X0, flanaganDerivn);
            } else {
                Xn = rungeKuttaFixedFour(RungeKuttaParameter.Step, T0, Tn, X0, flanaganDerivn);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if(Xn != null) {
            Xn = XTool.formatX(Xn);
        }
        return Xn;
    }

    private double[] rungeKuttaFixedFour(double h, double T0, double Tn, double[] X0, DerivnFunction derivnFunction) {
        RungeKutta rk = new RungeKutta();
        rk.setInitialValueOfX(T0);
        rk.setFinalValueOfX(Tn);
        rk.setInitialValuesOfY(X0);
        rk.setStepSize(h);
        return rk.fourthOrder(derivnFunction);
    }

    private double[] rungeKuttaAdaptiveFour(double h, double T0, double Tn, double[] X0, DerivnFunction derivnFunction) {
        RungeKutta rk = new RungeKutta();
        rk.setInitialValueOfX(T0);
        rk.setFinalValueOfX(Tn);
        rk.setInitialValuesOfY(X0);
        rk.setStepSize(h);
        rk.setToleranceScalingFactor(RungeKuttaParameter.tolerance_scaling_factor);
        rk.setToleranceAdditionFactor(RungeKuttaParameter.tolerance_addition_factor);
        return rk.cashKarp(derivnFunction);
    }

    private void init(List<EMUReaction> emuReactions, Map<String,Double> fluxPartialDifferentialMap) {
        interReactionInitX = XTool.computeInterReactionInitX(interReactionSizeEMUsMap);
        inputReactionInitX = XTool.computeInputReactionInitX(inputReactionSizeEMUsMap);
        ReactionCoefficientMatrixElement reactionCoefficientMatrixElement = ReactionCoefficientMatrixElementTool.
                computeReactionCoefficientMatrixElement(emuReactions, allInterReactionEMUsMap, sbCalculator);
        baseDerivn = new BaseDerivn();
        baseDerivn.setFluxPartialDifferentialMap(fluxPartialDifferentialMap);
        baseDerivn.setInputReactionInitX(inputReactionInitX);
        baseDerivn.setInterReactionXLength(interReactionInitX.length);
        baseDerivn.setReactionCoefficientMatrixElement(reactionCoefficientMatrixElement);
    }

    public void updateReactionCoefficientMatrixElement(Map<String, Double> reactionNameValueMap) {
        ReactionCoefficientMatrixElement reactionCoefficientMatrixElement = baseDerivn.getReactionCoefficientMatrixElement();
        List<CoefficientMatrixElement> tmpList = new ArrayList<>();
        tmpList.addAll(reactionCoefficientMatrixElement.getInputReactionEMUElementList());
        tmpList.addAll(reactionCoefficientMatrixElement.getSingleInterReactionEMUElementList());
        tmpList.addAll(reactionCoefficientMatrixElement.getConsumptionReactionEMUElementList());

        for(CoefficientMatrixElement element : tmpList) {
            element.setFluxValue(reactionNameValueMap.get(element.getrName()));
        }

        EMUCompositionCoefficientMatrixElement emuCompositionCoefficientMatrixElement = reactionCoefficientMatrixElement.getEmuCompositionCoefficientMatrixElement();
        int size = emuCompositionCoefficientMatrixElement.getEmu1CoefficientMatrixElementList().size();
        for(int index = 0; index < size; index++) {
            CoefficientMatrixElement element1 = emuCompositionCoefficientMatrixElement.getEmu1CoefficientMatrixElement(index);
            CoefficientMatrixElement element2 = emuCompositionCoefficientMatrixElement.getEmu2CoefficientMatrixElement(index);
            element1.setFluxValue(reactionNameValueMap.get(element1.getrName()));
            element2.setFluxValue(reactionNameValueMap.get(element2.getrName()));
        }

    }

    public SizeBaseCalculator getSbCalculator() {
        return sbCalculator;
    }

}
