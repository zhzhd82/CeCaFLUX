package edu.gyu.mfa.reaction;

import edu.gyu.mfa.emu.EMU;
import java.util.List;
import java.util.Map;

public abstract class AbsReactionSizeEMU {

    public Map<Integer, List<EMU>> interReactionSizeEMUsMap;
    public Map<Integer, List<EMU>> inputReactionSizeEMUsMap;
    public Map<Integer, List<EMU>> outputReactionSizeEMUsMap;
    public Map<Integer, Map<String, Integer>> interReactionSizeEMUPositionMap;
    public Map<Integer, Map<String, Integer>> inputReactionSizeEMUPositionMap;
    public Map<String, EMU> allInterReactionEMUsMap;
    public Map<String, String> compNameCarbonMap;

    public AbsReactionSizeEMU(ReactionSizeEMU reactionSizeEMU) {

        interReactionSizeEMUsMap = reactionSizeEMU.getInterReactionSizeEMUsMap();
        inputReactionSizeEMUsMap = reactionSizeEMU.getInputReactionSizeEMUsMap();
        outputReactionSizeEMUsMap = reactionSizeEMU.getOutputReactionSizeEMUsMap();
        interReactionSizeEMUPositionMap = reactionSizeEMU.getInterReactionSizeEMUPositionMap();
        inputReactionSizeEMUPositionMap = reactionSizeEMU.getInputReactionSizeEMUPositionMap();
        allInterReactionEMUsMap = reactionSizeEMU.getAllInterReactionEMUsMap();
        compNameCarbonMap = reactionSizeEMU.getCompNameCarbonMap();

    }

}
