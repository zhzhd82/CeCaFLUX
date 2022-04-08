package edu.gyu.mfa.reaction;

import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.emu.EMUTool;

import java.util.*;

public class ReactionSizeEMU {

    public Map<Integer, List<EMU>> interReactionSizeEMUsMap;
    public Map<Integer, List<EMU>> inputReactionSizeEMUsMap;
    public Map<Integer, List<EMU>> outputReactionSizeEMUsMap;
    public Map<Integer, Map<String, Integer>> interReactionSizeEMUPositionMap;
    public Map<Integer, Map<String, Integer>> inputReactionSizeEMUPositionMap;
    public Map<String, EMU> allInterReactionEMUsMap;
    public Map<String, String> compNameCarbonMap;

    public void generateSizeEMUs(List<EMUReaction> emuReactions) {
        interReactionSizeEMUsMap = new HashMap<>();
        inputReactionSizeEMUsMap = new HashMap<>();
        outputReactionSizeEMUsMap = new HashMap<>();
        allInterReactionEMUsMap = new HashMap<>();
        Map<String, Integer> inputEMUMap = new HashMap<>();
        Map<String, Integer> interEMUMap = new HashMap<>();
        Map<String, Integer> outputEMUMap = new HashMap<>();

        for (EMUReaction emuReaction : emuReactions) {
            if (ReactionTool.isInputReaction(emuReaction)) {
                for (EMU reactantEMU : emuReaction.getReactants()) {
                    if (!EMUTool.checkAndAddEMUToMap(reactantEMU, inputEMUMap)) {
                        appendEMUToTarget(reactantEMU, inputReactionSizeEMUsMap);
                    }
                }
            } else if (ReactionTool.isOutputReaction(emuReaction)) {
                for (EMU productEMU : emuReaction.getProducts()) {
                    if (!EMUTool.checkAndAddEMUToMap(productEMU, outputEMUMap)) {
                        appendEMUToTarget(productEMU, outputReactionSizeEMUsMap);
                    }
                }
            } else {
                List<EMU> tmp = new ArrayList<>();
                tmp.addAll(emuReaction.getReactants());
                tmp.addAll(emuReaction.getProducts());
                for (EMU emu : tmp) {
                    if (!EMUTool.checkAndAddEMUToMap(emu, interEMUMap)) {
                        appendEMUToTarget(emu, interReactionSizeEMUsMap);
                    }
                }
            }
        }
        sortEMUMap(interReactionSizeEMUsMap, inputReactionSizeEMUsMap, outputReactionSizeEMUsMap);
    }

    private void appendEMUToTarget(EMU emu, Map<Integer, List<EMU>> map) {
        int size = emu.getSize();
        List<EMU> targetEMUList = map.get(size);
        if(targetEMUList == null) {
            targetEMUList = new ArrayList<>();
            map.put(size, targetEMUList);
        }
        targetEMUList.add(emu);
    }

    private void sortEMUMap(Map<Integer, List<EMU>>... maps) {
        for(Map<Integer, List<EMU>> map : maps) {
            for (Integer key : map.keySet()) {
                Collections.sort(map.get(key));
            }
        }
    }

    public void computeInputReactionEMUPosition() {
        inputReactionSizeEMUPositionMap = computeSizeEMUPosition(inputReactionSizeEMUsMap);
    }

    public void computeInterReactionEMUPosition() {
        interReactionSizeEMUPositionMap = computeSizeEMUPosition(interReactionSizeEMUsMap);
    }

    private Map<Integer, Map<String, Integer>> computeSizeEMUPosition(Map<Integer, List<EMU>> sizeEMUsMap) {
        Map<Integer, Map<String, Integer>> sizeEMUPositionMap = new HashMap<>();
        for(Integer size : sizeEMUsMap.keySet()) {
            Map<String, Integer> map = new HashMap<>();
            int position = 0;
            for (EMU emu : sizeEMUsMap.get(size)) {
                map.put(emu.getKey(), position++);
            }
            sizeEMUPositionMap.put(size, map);
        }
        return sizeEMUPositionMap;
    }

    public Map<Integer, List<EMU>> getInterReactionSizeEMUsMap() {
        return interReactionSizeEMUsMap;
    }

    public void setInterReactionSizeEMUsMap(Map<Integer, List<EMU>> interReactionSizeEMUsMap) {
        this.interReactionSizeEMUsMap = interReactionSizeEMUsMap;
        sortEMUMap(interReactionSizeEMUsMap);
        allInterReactionEMUsMap = new HashMap<>();
        for(int size : interReactionSizeEMUsMap.keySet()) {
            for(EMU emu : interReactionSizeEMUsMap.get(size)) {
                allInterReactionEMUsMap.put(emu.getKey(), emu);
            }
        }
    }

    public void computeCompoundNameCarbon(List<Reaction> reactions) {
        compNameCarbonMap = new HashMap<>();
        for(Reaction reaction : reactions) {
            for(Compound reactant : reaction.getReactants()) {
                compNameCarbonMap.put(reactant.getName(), reactant.getCarbon());
            }
            for(Compound product : reaction.getProducts()) {
                compNameCarbonMap.put(product.getName(), product.getCarbon());
            }
        }
    }

    public Map<Integer, List<EMU>> getInputReactionSizeEMUsMap() {
        return inputReactionSizeEMUsMap;
    }

    public Map<Integer, List<EMU>> getOutputReactionSizeEMUsMap() {
        return outputReactionSizeEMUsMap;
    }

    public Map<Integer, Map<String, Integer>> getInterReactionSizeEMUPositionMap() {
        return interReactionSizeEMUPositionMap;
    }

    public Map<Integer, Map<String, Integer>> getInputReactionSizeEMUPositionMap() {
        return inputReactionSizeEMUPositionMap;
    }

    public Map<String, EMU> getAllInterReactionEMUsMap() {
        return allInterReactionEMUsMap;
    }

    public Map<String, String> getCompNameCarbonMap() {
        return compNameCarbonMap;
    }
}
