package edu.gyu.mfa.info;

import edu.gyu.mfa.calculator.SizeBaseCalculator;
import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.x.XTool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CAndCFree {

    private Map<String, Double> C;
    private Map<String, Double> C_Free;
    private List<String> C_FreeList;
    private List<String> C_List;
    private Map<Integer, Double> C_PosValueMap;
    private Map<Integer, Double> CLCJ_PosValueMap;

    public CAndCFree(Map<String, Double> poolSizeMap, Map<String, EMU> interReactionEMUMap) {
        selectCAndCFree(poolSizeMap, interReactionEMUMap);
    }

    public void selectCAndCFree(Map<String, Double> poolSizeMap, Map<String, EMU> interReactionEMUMap) {
        C = new HashMap<>();
        C_List = new ArrayList<>();
        C_FreeList = new ArrayList<>();
        Map<String, Integer> interReactionCompNameMap = new HashMap<>();
        for(String key : interReactionEMUMap.keySet()) {
            interReactionCompNameMap.put(interReactionEMUMap.get(key).getName(), 1);
        }
        for(String compName : interReactionCompNameMap.keySet()) {
            if(!poolSizeMap.containsKey(compName)) {
                C_FreeList.add(compName);
            }
        }
        C_List.addAll(C_FreeList);
        for(String compName : poolSizeMap.keySet()) {
            C.put(compName, poolSizeMap.get(compName));
            C_List.add(compName);
        }
        Count.cCount = C_List.size();
        Count.cFreeCount = C_FreeList.size();
        C_Free = new HashMap<>();
    }

    public void initCAndCFree(double[] cFreeValues, Map<String, EMU> allInterReactionEMUsMap, SizeBaseCalculator sbCalculator) {
        for (int index = 0; index < Count.cFreeCount; index++) {
            C_Free.put(C_FreeList.get(index), cFreeValues[index]);
            C.put(C_FreeList.get(index), cFreeValues[index]);
        }
        computeCAndCFreePosValue(allInterReactionEMUsMap, sbCalculator);
    }

    private void computeCAndCFreePosValue(Map<String, EMU> allInterReactionEMUsMap, SizeBaseCalculator sbCalculator) {
        C_PosValueMap = new HashMap<>();
        CLCJ_PosValueMap = new HashMap<>();
        int xLength = XTool.computeXLengthByEMU(allInterReactionEMUsMap);
        for(String emuKey : allInterReactionEMUsMap.keySet()) {
            EMU emu = allInterReactionEMUsMap.get(emuKey);
            int base = sbCalculator.findInterReactionEMUBase(emu);
            double value = C.get(emu.getName());
            for(int mIndex = 0; mIndex <= emu.getSize(); mIndex++) {
                C_PosValueMap.put(base + mIndex, value);
            }
            for (int vFreeIndex = 0; vFreeIndex < Count.freeCount; vFreeIndex++) {
                for(int mIndex = 0; mIndex <= emu.getSize(); mIndex++) {
                    C_PosValueMap.put(base + (vFreeIndex + 1) * xLength + mIndex, value);
                }
            }
            for (int cIndex = 0; cIndex < Count.cCount; cIndex++) {
                for(int mIndex = 0; mIndex <= emu.getSize(); mIndex++) {
                    int cPos = base + mIndex + (cIndex + 1 + Count.freeCount) * xLength;
                    C_PosValueMap.put(cPos, value);
                    if(emu.getName().equals(C_List.get(cIndex))) {
                        CLCJ_PosValueMap.put(cPos, value);
                    }
                }
            }
        }
    }

    public Map<Integer, Double> getC_PosValueMap() {
        return C_PosValueMap;
    }

    public Map<Integer, Double> getCloneC_PosValueMap() {
        Map<Integer, Double> result = new HashMap<>();
        for(Integer key : C_PosValueMap.keySet()) {
            result.put(key, C_PosValueMap.get(key));
        }
        return result;
    }

    public Map<Integer, Double> getCloneCLCJ_PosValueMap() {
        Map<Integer, Double> result = new HashMap<>();
        for(Integer key : CLCJ_PosValueMap.keySet()) {
            result.put(key, CLCJ_PosValueMap.get(key));
        }
        return result;
    }

    public List<String> getC_FreeList() {
        return C_FreeList;
    }

}
