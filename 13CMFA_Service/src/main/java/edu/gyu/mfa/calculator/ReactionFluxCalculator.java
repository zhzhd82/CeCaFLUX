package edu.gyu.mfa.calculator;

import Jama.Matrix;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.matrix.MatrixConsFreeInfo;
import edu.gyu.mfa.matrix.MatrixTool;
import edu.gyu.mfa.matrix.VectorTool;
import edu.gyu.mfa.reaction.Reaction;
import edu.gyu.mfa.util.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactionFluxCalculator {

    private MatrixConsFreeInfo consFreeInfo;

    public ReactionFluxCalculator(MatrixConsFreeInfo _consFreeInfo) {
        consFreeInfo = _consFreeInfo;
    }

    public double[] calculateConsDeviationByFree(double[] freeDeviation) {
        double[][] sConsArray = consFreeInfo.getsConsArray();
        double[][] sFreeArray = consFreeInfo.getsFreeArray();
        double[][] Sdf = new Matrix(sConsArray).inverse().times(new Matrix(sFreeArray)).times(-1).getArray();
        int rowNum = Sdf.length;
        int colNum = Sdf[0].length;
        double[] consDeviation = new double[rowNum];
        double[] Ef = new double[freeDeviation.length];
        for(int index = 0; index < freeDeviation.length; index++) {
            Ef[index] = freeDeviation[index] * freeDeviation[index];
        }
        for(int row = 0; row < rowNum; row++) {
            double sum = 0;
            for(int col = 0; col < colNum; col++) {
                sum += Sdf[row][col] * Sdf[row][col] * Ef[col];
            }
            consDeviation[row] = Math.pow(sum, 0.5);
        }
        return consDeviation;
    }

    public void calculateReactionFlux(List<Reaction> reactions, double[] b, double[] vFreeValues) {
        double[] Vp = calculateV(b, vFreeValues);
        for (int index = 0; index < reactions.size(); index++) {
            reactions.get(index).setFlux(Vp[index]);
        }
    }

    public Map<String, Double> calculateReactionFluxMap(List<Reaction> reactions, double[] b, double[] vFreeValues) {
        Map<String, Double> reactionNameValueMap = new HashMap<>();
        double[] Vp = calculateV(b, vFreeValues);
        for (int index = 0; index < reactions.size(); index++) {
            reactionNameValueMap.put(reactions.get(index).getName(), Vp[index]);
        }
        return reactionNameValueMap;
    }

    private double[] calculateV(double[] b, double[] vFreeValues) {
        double[][] sConsArray = consFreeInfo.getsConsArray();
        double[][] sFreeArray = consFreeInfo.getsFreeArray();
        Map<Integer, Integer> sConsMap = consFreeInfo.getsConsMap();

        Matrix matB = new Matrix(VectorTool.convertVectorToColArray(b));
        Matrix matSiVFree = new Matrix(sFreeArray).times(new Matrix(VectorTool.convertVectorToColArray(vFreeValues)));

        Matrix mat = matB.minus(matSiVFree);
        Matrix sConsMatrix = new Matrix(sConsArray);
        Matrix sConsInverseMatrix = sConsMatrix.inverse();
        double[][] data = sConsInverseMatrix.times(mat).getArray();

        int vConsIndex = 0, vFreeIndex = 0;
        int numCol = sConsArray[0].length + sFreeArray[0].length;
        double[] v = new double[numCol];
        for (int col = 0; col < numCol; col++) {
            if (sConsMap.containsKey(col)) {
                v[col] = data[vConsIndex++][0];
            } else {
                v[col] = vFreeValues[vFreeIndex++];
            }
        }
        return v;
    }

    public void calculateReactionConsFreeInfo(List<Reaction> reactions) {
        Map<Integer, Integer> sConsMap = consFreeInfo.getsConsMap();
        Count.consCount = 0;
        Count.freeCount = 0;
        for (int col = 0; col < reactions.size(); col++) {
            Reaction reaction = reactions.get(col);
            if (sConsMap.containsKey(col)) {
                reaction.setConsed(true);
                reaction.setIndex(Count.consCount++);
            } else {
                reaction.setConsed(false);
                reaction.setIndex(Count.freeCount++);
            }
        }
    }

    public Map<String,Double> calculatePartialDifferential() {
        Map<String,Double> result = new HashMap<>();
        double[][] sConsArray = consFreeInfo.getsConsArray();
        double[][] sFreeArray = consFreeInfo.getsFreeArray();
        for(int freeIndex = 0; freeIndex < Count.freeCount; freeIndex++) {
            double[] freePartialDiff = new double[Count.freeCount];
            for(int freePartialDiffIndex = 0; freePartialDiffIndex < Count.freeCount; freePartialDiffIndex++) {
                if(freePartialDiffIndex == freeIndex) {
                    freePartialDiff[freePartialDiffIndex] = -1;
                } else {
                    freePartialDiff[freePartialDiffIndex] = 0;
                }
            }
            double[] rhsArray = MatrixTool.matrixTimesVector(sFreeArray, freePartialDiff);
            Matrix lhs = new Matrix(sConsArray);
            Matrix rhs = new Matrix(rhsArray,rhsArray.length);
            Matrix ans = lhs.solve(rhs);
            double[][] data = ans.getArray();
            for(int consIndex = 0; consIndex < Count.consCount; consIndex++) {
                result.put(Util.joinNames(Constant.KEY_SPLITTER, consIndex, freeIndex), data[consIndex][0]);
            }
        }
        return result;
    }

}
