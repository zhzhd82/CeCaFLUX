package cn.gyu.flux.reaction;

import java.util.List;

public class FluxEquationMatrix extends CompReactPos{

    private double[][] stoichiometricMatrix;

    public FluxEquationMatrix() {
        super();
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

    public double[][] getStoichiometricMatrix() {
        return stoichiometricMatrix;
    }

}
