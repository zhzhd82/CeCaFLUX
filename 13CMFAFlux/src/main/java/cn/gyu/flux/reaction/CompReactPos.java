package cn.gyu.flux.reaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompReactPos {

    protected Map<String, Integer> compoundPositionMap;
    protected Map<String, Integer> reactionPositionMap;

    public CompReactPos() {
        compoundPositionMap = new HashMap<>();
        reactionPositionMap = new HashMap<>();
    }

    public void calculatePosition(List<Reaction> reactions, boolean isInterReactionCompound) {
        compoundPositionMap.clear();
        reactionPositionMap.clear();
        int reactionPosition = 0, compoundPosition = 0;
        for (Reaction reaction : reactions) {
            if (reactionPositionMap.containsKey(reaction.getName())) {
                continue;
            }
            reactionPositionMap.put(reaction.getName(), reactionPosition++);
            if (isInterReactionCompound) {
                if (!reaction.getType().contains(Constant.INTER_REACTION)) {
                    continue;
                }
            }
            List<Compound> tmp = new ArrayList<>();
            tmp.addAll(reaction.getReactants());
            tmp.addAll(reaction.getProducts());
            for(Compound comp : tmp) {
                if (!compoundPositionMap.containsKey(comp.getName())) {
                    compoundPositionMap.put(comp.getName(), compoundPosition++);
                }
            }
        }
    }

    public Map<String, Integer> getCompoundPositionMap() {
        return compoundPositionMap;
    }

    public Map<String, Integer> getReactionPositionMap() {
        return reactionPositionMap;
    }
}
