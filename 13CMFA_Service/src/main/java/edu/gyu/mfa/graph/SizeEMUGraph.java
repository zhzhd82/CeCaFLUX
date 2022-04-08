package edu.gyu.mfa.graph;

import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.emu.EMUTool;
import edu.gyu.mfa.ftbl.FTBLFile;
import edu.gyu.mfa.ftbl.MassSpectrometry;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.reaction.EMUReaction;
import edu.gyu.mfa.reaction.ReactionSizeEMU;
import edu.gyu.mfa.reaction.ReactionTool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SizeEMUGraph {

    private ReactionSizeEMU reactionSizeEMU;
    private Map<Integer, String> positionSizeEMUMap;
    private Map<String, Integer> sizeEMUPositionMap;
    private Map<String, Integer> adjacentMatrixMap;

    public SizeEMUGraph(ReactionSizeEMU _reactionSizeEMU) {
        reactionSizeEMU = _reactionSizeEMU;
        computeSizeEMUPosition();
    }

    private void computeSizeEMUPosition() {
        positionSizeEMUMap = new HashMap<>();
        Map<Integer, List<EMU>> sizeEMUsMap = reactionSizeEMU.getInterReactionSizeEMUsMap();
        int index = 0;
        int maxSize = EMUTool.computeMaxSize(sizeEMUsMap);
        for (int size = 1; size <= maxSize; size++) {
            if(sizeEMUsMap.containsKey(size)) {
                List<EMU> emus = sizeEMUsMap.get(size);
                for(EMU emu : emus) {
                    positionSizeEMUMap.put(index++, emu.getKey());
                }
            }
        }
        sizeEMUPositionMap = new HashMap<>();
        for (Integer key : positionSizeEMUMap.keySet()) {
            sizeEMUPositionMap.put(positionSizeEMUMap.get(key), key);
        }
    }

    private void computeAdjacentMatrixMap(List<EMUReaction> emuReactions) {
        adjacentMatrixMap = new HashMap<>();
        for(EMUReaction emuReaction : emuReactions) {
            if(ReactionTool.isInterReaction(emuReaction)
                    && ReactionTool.isOneProductReaction(emuReaction)) {
                for(EMU rEMU : emuReaction.getReactants()) {
                    for(EMU pEMU : emuReaction.getProducts()) {
                        int row = sizeEMUPositionMap.get(rEMU.getKey());
                        int col = sizeEMUPositionMap.get(pEMU.getKey());
                        adjacentMatrixMap.put(row + Constant.KEY_SPLITTER + col, 1);
                    }
                }
            }
        }

    }

    private void filterSizeEMU(FTBLFile ftblFile, Map<Integer, List<Integer>> nodeNeighbors) {
        Map<Integer, Integer> reservedPositionMap = new HashMap<>();
        List<String> measuredEMUList = getMeasuredEMU(ftblFile);
        for (String measuredEMUKey : measuredEMUList) {
            int col = sizeEMUPositionMap.get(measuredEMUKey);
            for (Integer row : positionSizeEMUMap.keySet()) {
                if(reservedPositionMap.containsKey(row)) {
                    continue;
                }
                if(GraphTool.hasPath(row, col, nodeNeighbors)) {
                    reservedPositionMap.put(row, 1);
                }
            }
        }
        reactionSizeEMU.setInterReactionSizeEMUsMap(filterSizeEMUByPosition(reservedPositionMap));
        reactionSizeEMU.computeInterReactionEMUPosition();
    }

    private Map<Integer, List<EMU>> filterSizeEMUByPosition(Map<Integer, Integer> retainedPositionMap) {
        Map<Integer, List<EMU>> tmp = new HashMap<>();
        Map<Integer, List<EMU>> sizeEMUsMap = reactionSizeEMU.getInterReactionSizeEMUsMap();
        for(int position : retainedPositionMap.keySet()) {
            String sizeEMUKey = positionSizeEMUMap.get(position);
            int size = EMUTool.parseSizeFromEMUKey(sizeEMUKey);
            List<EMU> emus = sizeEMUsMap.get(size);
            List<EMU> targetEMUs = tmp.get(size);
            if(targetEMUs == null) {
                targetEMUs = new ArrayList<>();
                tmp.put(size, targetEMUs);
            }
            for(EMU emu : emus) {
                if(emu.getKey().equals(sizeEMUKey)) {
                    targetEMUs.add(emu);
                    break;
                }
            }
        }
        return tmp;
    }

    public void filterSizeEMU(List<EMUReaction> emuReactions, FTBLFile ftblFile) {
        computeAdjacentMatrixMap(emuReactions);
        Map<Integer, List<Integer>> nodeNeighbors = GraphTool.computeGraphNodeNeighbors(adjacentMatrixMap);
        filterSizeEMU(ftblFile, nodeNeighbors);
    }

    private List<String> getMeasuredEMU(FTBLFile ftblFile) {
        List<String> measuredEMUList = new ArrayList<>();
        Map<String, Integer> checkMap = new HashMap<>();
        for(MassSpectrometry massSpectrometry : ftblFile.getMassSpectrometryList()) {
            String emuKey = massSpectrometry.getEMUKey();
            if(checkMap.containsKey(emuKey)) {
                continue;
            }
            checkMap.put(emuKey, 1);
            measuredEMUList.add(emuKey);
        }
        return measuredEMUList;
    }

}
