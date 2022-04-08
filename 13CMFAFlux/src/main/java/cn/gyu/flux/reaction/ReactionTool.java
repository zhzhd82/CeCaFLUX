package cn.gyu.flux.reaction;

import java.util.ArrayList;
import java.util.List;

public class ReactionTool {

    public static List<Reaction> parseNetwork(String reactionFormulas) {
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        List<Reaction> reactions = new ArrayList<>();

        String[] formulaSplits = reactionFormulas.split(outerSplitter);
        for(String formula : formulaSplits) {
            Reaction reaction = new Reaction();
            reactions.add(reaction);
            String[] partSplits = formula.split(innerSplitter);
            reaction.setName(partSplits[0].trim());

            String[] compSplits = partSplits[1].split("\\s-->\\s");
            String[] reactantSplits = compSplits[0].split("\\s\\+\\s");
            String[] productSplits = compSplits[1].split("\\s\\+\\s");
            for(String reactant : reactantSplits) {
                reaction.addReactant(new Compound(reactant, ""));
            }
            for(String product : productSplits) {
                reaction.addProduct(new Compound(product, ""));
            }
            if (reaction.getReactants().size() == 1) {
                reaction.addType(Constant.SINGLE_REACTION);
            } else {
                reaction.addType(Constant.MULTI_REACTION);
            }
        }
        return reactions;
    }

}
