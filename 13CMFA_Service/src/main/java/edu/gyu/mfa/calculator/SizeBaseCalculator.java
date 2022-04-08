package edu.gyu.mfa.calculator;

import edu.gyu.mfa.emu.EMU;
import java.util.*;

public class SizeBaseCalculator {

    private Map<Integer, List<EMU>> interReactionSizeEMUsMap;
    private Map<Integer, List<EMU>> inputReactionSizeEMUsMap;

    public SizeBaseCalculator(Map<Integer, List<EMU>> interReactionSizeEMUsMap,
                              Map<Integer, List<EMU>> inputReactionSizeEMUsMap) {
        this.interReactionSizeEMUsMap = interReactionSizeEMUsMap;
        this.inputReactionSizeEMUsMap = inputReactionSizeEMUsMap;
    }

    private Map<String, Integer> interReactionEMUBaseMap = new HashMap<>();
    public int findInterReactionEMUBase(EMU emu) {
        if(interReactionEMUBaseMap.containsKey(emu.getKey())) {
            return interReactionEMUBaseMap.get(emu.getKey());
        }
        int base = findEMUBase(interReactionSizeEMUsMap, emu);
        interReactionEMUBaseMap.put(emu.getKey(), base);
        return base;
    }

    private Map<String, Integer> inputReactionEMUBaseMap = new HashMap<>();
    public int findInputReactionEMUBase(EMU emu) {
        if(inputReactionEMUBaseMap.containsKey(emu.getKey())) {
            return inputReactionEMUBaseMap.get(emu.getKey());
        }
        int base = findEMUBase(inputReactionSizeEMUsMap, emu);
        inputReactionEMUBaseMap.put(emu.getKey(), base);
        return base;
    }

    private int findEMUBase(Map<Integer, List<EMU>> sizeEMUsMap, EMU emu) {
        int base = 0;
        for(int size = 1; size <= emu.getSize(); size++) {
            if(sizeEMUsMap.containsKey(size)) {
                List<EMU> emuList = sizeEMUsMap.get(size);
                for(EMU tmpEMU : emuList) {
                    if(tmpEMU.getKey().equals(emu.getKey())) {
                        return base;
                    } else {
                        base = base + tmpEMU.getSize() + 1;
                    }
                }
            }
        }
        return base;
    }

}
