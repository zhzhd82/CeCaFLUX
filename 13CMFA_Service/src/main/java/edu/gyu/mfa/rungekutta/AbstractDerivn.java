package edu.gyu.mfa.rungekutta;

import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.matrix.CoefficientMatrixElement;
import edu.gyu.mfa.matrix.EMUCompositionCoefficientMatrixElement;
import edu.gyu.mfa.reaction.Flux;
import edu.gyu.mfa.reaction.ReactionCoefficientMatrixElement;
import edu.gyu.mfa.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class AbstractDerivn {

    private Map<Integer, Double> C_PosValueMap;
    private Map<Integer, Double> CLCJ_PosValueMap;
    private Map<String, Double> fluxPartialDifferentialMap;
    private double[] inputReactionInitX;
    private ReactionCoefficientMatrixElement reactionCoefficientMatrixElement;
    private int xLength;
    private boolean isCalculatingDxdw;

    private int dxdcStartIndex;

    public AbstractDerivn(BaseDerivn baseDerivn) {
        fluxPartialDifferentialMap = baseDerivn.getFluxPartialDifferentialMap();
        inputReactionInitX = baseDerivn.getInputReactionInitX();
        reactionCoefficientMatrixElement = baseDerivn.getReactionCoefficientMatrixElement();
        xLength = baseDerivn.getInterReactionXLength();
        dxdcStartIndex = (Count.freeCount + 1) * xLength;
        isCalculatingDxdw = false;
    }

    public double[] baseDerivn(double t, double[] x) {
        double[] dxdt = new double[x.length];
        try{
            double[] sizeEMUX;
            for(CoefficientMatrixElement element : reactionCoefficientMatrixElement.getInputReactionEMUElementList()) {
                sizeEMUX = getSizeEMUX(element, inputReactionInitX);
                multiplyFluxSizeEMUX(element, element.getFluxValue(), sizeEMUX, dxdt);
            }

            for(CoefficientMatrixElement element : reactionCoefficientMatrixElement.getSingleInterReactionEMUElementList()) {
                sizeEMUX = getSizeEMUX(element, x);
                multiplyFluxSizeEMUX(element, element.getFluxValue(), sizeEMUX, dxdt);

                if(isCalculatingDxdw) {
                    for(int k = 0; k < Count.freeCount; k++) {
                        double dvdk = getDvdk(element.getFlux(), k);
                        calculateDxdv(element,sizeEMUX, x, element.getFluxValue(), dvdk, (k + 1) * xLength, dxdt);
                    }
                    for(int k = 0; k < Count.cCount; k++) {
                        calculateDxdc(element,sizeEMUX, x, element.getFluxValue(),(k + 1 + Count.freeCount) * xLength, dxdt);
                    }
                }
            }

            for(CoefficientMatrixElement element : reactionCoefficientMatrixElement.getConsumptionReactionEMUElementList()) {
                sizeEMUX = getSizeEMUX(element, x);
                double fluxValue = element.getFluxValue() * -1;
                multiplyFluxSizeEMUX(element, fluxValue, sizeEMUX, dxdt);

                if(isCalculatingDxdw) {
                    for(int k = 0; k < Count.freeCount; k++) {
                        double dvdk = getDvdk(element.getFlux(), k);
                        calculateDxdv(element, sizeEMUX, x, fluxValue, dvdk, (k + 1) * xLength, dxdt);
                    }
                    for(int k = 0; k < Count.cCount; k++) {
                        calculateDxdc(element,sizeEMUX, x, fluxValue,(k + 1 + Count.freeCount) * xLength, dxdt);
                    }
                }

            }

            EMUCompositionCoefficientMatrixElement compositionCoefficientMatrixElement =
                    reactionCoefficientMatrixElement.getEmuCompositionCoefficientMatrixElement();
            List<CoefficientMatrixElement>  emu1CoefficientMatrixElementList =
                    compositionCoefficientMatrixElement.getEmu1CoefficientMatrixElementList();
            List<CoefficientMatrixElement>  emu2CoefficientMatrixElementList =
                    compositionCoefficientMatrixElement.getEmu2CoefficientMatrixElementList();
            for(int index = 0; index < emu1CoefficientMatrixElementList.size(); index++) {
                CoefficientMatrixElement element1 = emu1CoefficientMatrixElementList.get(index);
                CoefficientMatrixElement element2 = emu2CoefficientMatrixElementList.get(index);
                double[] x1 = getSizeEMUX(element1, x);
                double[] x2 = getSizeEMUX(element2, x);
                sizeEMUX = getEMUCompositionX(x1, x2);
                double fluxValue = element1.getFluxValue();
                multiplyFluxSizeEMUX(element1, fluxValue, sizeEMUX, dxdt);

                if(isCalculatingDxdw) {
                    int dxdvIndexBase;
                    for (int k = 0; k < Count.freeCount; k++) {
                        double dvdk = getDvdk(element1.getFlux(), k);
                        dxdvIndexBase = (k + 1) * xLength + element1.gettBase();
                        for(int vIndex = 0; vIndex < sizeEMUX.length; vIndex++) {
                            dxdt[dxdvIndexBase + vIndex] += dvdk * sizeEMUX[vIndex];
                        }
                        double[] dxdv_x2 = getDxdwX(element2, (k + 1) * xLength + element2.getmBase(), x);
                        double[] dxdv_composition_x = getEMUCompositionX(x1, dxdv_x2);
                        for(int vIndex = 0; vIndex < dxdv_composition_x.length; vIndex++) {
                            dxdt[dxdvIndexBase + vIndex] += fluxValue * dxdv_composition_x[vIndex];
                        }
                        double[] dxdv_x1 = getDxdwX(element1, (k + 1) * xLength + element1.getmBase(), x);
                        dxdv_composition_x = getEMUCompositionX(dxdv_x1, x2);
                        for(int vIndex = 0; vIndex < dxdv_composition_x.length; vIndex++) {
                            dxdt[dxdvIndexBase + vIndex] += fluxValue * dxdv_composition_x[vIndex];
                        }
                    }
                    int dxdcIndexBase;
                    for (int k = 0; k < Count.cCount; k++) {
                        dxdcIndexBase = (k + 1 + Count.freeCount) * xLength + element1.gettBase();
                        double[] dxdc_x1 = getDxdwX(element1, (k + 1 + Count.freeCount) * xLength + element1.getmBase(), x);
                        double[] dxdc_composition_x = getEMUCompositionX(dxdc_x1, x2);
                        for(int vIndex = 0; vIndex < dxdc_composition_x.length; vIndex++) {
                            dxdt[dxdcIndexBase + vIndex] += fluxValue * dxdc_composition_x[vIndex];
                        }
                        double[] dxdc_x2 = getDxdwX(element2, (k + 1 + Count.freeCount) * xLength + element2.getmBase(), x);
                        dxdc_composition_x = getEMUCompositionX(x1, dxdc_x2);
                        for(int vIndex = 0; vIndex < dxdc_composition_x.length; vIndex++) {
                            dxdt[dxdcIndexBase + vIndex] += fluxValue * dxdc_composition_x[vIndex];
                        }
                    }
                }

            }

            for (int index = 0; index < dxdt.length; index++) {
                if(index >= dxdcStartIndex && C_PosValueMap != null && C_PosValueMap.containsKey(index)
                        && CLCJ_PosValueMap != null && CLCJ_PosValueMap.containsKey(index)) {
                    int xtIndex = index;
                    while(xtIndex >= xLength) {
                        xtIndex -= xLength;
                    }
                    dxdt[index] = Util.formatNumber((dxdt[index] - dxdt[xtIndex]) / C_PosValueMap.get(index));
                } else {
                    dxdt[index] = Util.formatNumber(dxdt[index] / C_PosValueMap.get(index));
                }
            }
        }catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            String exception = baos.toString();
            System.out.println(exception);
        }
        return dxdt;
    }

    private void multiplyFluxSizeEMUX(CoefficientMatrixElement element, double fluxValue, double[] sizeEMUX, double[] dxdt) {
        int tBase = element.gettBase();
        for(int vIndex = 0; vIndex < sizeEMUX.length; vIndex++) {
            dxdt[tBase + vIndex] += fluxValue * sizeEMUX[vIndex];
        }

    }

    private double[] getSizeEMUX(CoefficientMatrixElement element, double[] x) {
        double[] result = new double[element.getSize() + 1];
        for(int vIndex = 0; vIndex <= element.getSize(); vIndex++) {
            result[vIndex] = x[element.getmBase() + vIndex];
        }
        return result;
    }

    private void calculateDxdv(CoefficientMatrixElement element, double[] sizeEMUX, double[] x, double fluxValue, double dvdk, int base, double[] dxdt) {
        for(int vIndex = 0; vIndex < sizeEMUX.length; vIndex++) {
            dxdt[base + element.gettBase() + vIndex] += dvdk * sizeEMUX[vIndex];
            dxdt[base + element.gettBase() + vIndex] += fluxValue * x[base + element.getmBase() + vIndex];
        }
    }

    private void calculateDxdc(CoefficientMatrixElement element, double[] sizeEMUX, double[] x, double fluxValue, int base, double[] dxdt) {
        for(int vIndex = 0; vIndex < sizeEMUX.length; vIndex++) {
            dxdt[base + element.gettBase() + vIndex] += fluxValue * x[base + element.getmBase() + vIndex];
        }
    }

    private double[] getDxdwX(CoefficientMatrixElement element, int base, double[] x) {
        double[] result = new double[element.getSize() + 1];
        for(int vIndex = 0; vIndex < result.length; vIndex++) {
            result[vIndex] = x[base + vIndex];
        }
        return result;
    }

    private double[] getEMUCompositionX(double[] vector1, double[] vector2) {
        int targetLength = vector1.length + vector2.length - 1;
        double[] vector = new double[targetLength];
        for (int size = 0; size < targetLength; size++) {
            double sum = 0;
            for (int size1 = 0; size1 <= size && size1 < vector1.length; size1++) {
                int size2 = size - size1;
                if(size2 < vector2.length) {
                    sum += vector1[size1] * vector2[size2];
                }
            }
            vector[size] = sum;
        }
        return vector;
    }

    private double getDvdk(Flux flux, int k) {
        double result = 0;
        if (flux.isConsed()) {
            result = fluxPartialDifferentialMap.get(Util.joinNames(Constant.KEY_SPLITTER, flux.getIndex(), k));
        } else if (flux.getIndex() == k) {
            result = 1;
        }
        return result;
    }

    public void setC_PosValueMap(Map<Integer, Double> C_PosValueMap) {
        this.C_PosValueMap = C_PosValueMap;
    }

    public void setCLCJ_PosValueMap(Map<Integer, Double> CLCJ_PosValueMap) {
        this.CLCJ_PosValueMap = CLCJ_PosValueMap;
    }

    public void setCalculatingDxdw(boolean calculatingDxdw) {
        isCalculatingDxdw = calculatingDxdw;
    }
}
