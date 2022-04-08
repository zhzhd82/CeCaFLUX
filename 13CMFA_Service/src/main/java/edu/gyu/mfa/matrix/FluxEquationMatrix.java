package edu.gyu.mfa.matrix;

import edu.gyu.mfa.compound.CompReactPos;
import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.ftbl.Equality;
import edu.gyu.mfa.ftbl.FTBLFile;
import edu.gyu.mfa.ftbl.FluxNet;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.reaction.Reaction;
import java.util.ArrayList;
import java.util.List;

public class FluxEquationMatrix extends CompReactPos{

    private double[][] stoichiometricMatrix;
    private double[][] Sa, Si;
    private double[] b;
    private FTBLFile ftblFile;

    public FluxEquationMatrix(FTBLFile ftblFile) {
        super();
        this.ftblFile = ftblFile;
    }

    public void computeStoichiometricMatrix(List<Reaction> reactions, boolean isInterReactionCompound) {
        calculatePosition(reactions, isInterReactionCompound);
        stoichiometricMatrix = new double[compoundPositionMap.size()][reactionPositionMap.size()];
        for (Reaction reaction : reactions) {
            for (Compound reactant : reaction.getReactants()) {
                if (isInterReactionCompound && reaction.getType().contains(Constant.INPUT_REACTION)) {
                    continue;
                }
                stoichiometricMatrix[compoundPositionMap.get(reactant.getName())][reactionPositionMap
                        .get(reaction.getName())] += -1;
            }
            for (Compound product : reaction.getProducts()) {
                if (isInterReactionCompound && reaction.getType().contains(Constant.OUTPUT_REACTION)) {
                    continue;
                }
                stoichiometricMatrix[compoundPositionMap.get(product.getName())][reactionPositionMap
                        .get(reaction.getName())] += 1;
            }
        }
    }

    public void computeSaAndSi() {
        List<Equality> equalityList = new ArrayList<>();
        equalityList.addAll(ftblFile.getEqualityList());
        for(FluxNet fluxNet : ftblFile.getFluxNetList()) {
            equalityList.add(new Equality(fluxNet.getValue(), fluxNet.getReaction_name(), 1));
        }
        int sRow = compoundPositionMap.size(), sCol = reactionPositionMap.size();
        int tRow = sRow + equalityList.size(), tCol = sCol + 1;
        Sa = new double[tRow][sCol];
        Si = new double[tRow][tCol];
        b = new double[tRow];
        for (int row = 0; row < sRow; row++) {
            for (int col = 0; col < sCol; col++) {
                Sa[row][col] = stoichiometricMatrix[row][col];
                Si[row][col] = stoichiometricMatrix[row][col];
            }
        }
        for (int index = 0; index < equalityList.size(); index++) {
            Equality equality = equalityList.get(index);
            int row = sRow + index;
            for(String name : equality.getKeySet()) {
                int col = reactionPositionMap.get(name);
                Sa[row][col] = equality.getCoefficient(name);
                Si[row][col] = equality.getCoefficient(name);
            }
            b[row] = equality.getValue();
            Si[row][tCol - 1] = b[row];
        }
    }

    public boolean checkByRank() {
        return MatrixTool.checkByRank(Sa, Si);
    }

    public double[][] getStoichiometricMatrix() {
        return stoichiometricMatrix;
    }

    public double[][] getSa() {
        return Sa;
    }

    public double[] getB() {
        return b;
    }

}
