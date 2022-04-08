package edu.gyu.mfa.x;

import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.emu.EMUTool;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.matrix.VectorTool;
import edu.gyu.mfa.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XTool {

    public static double[] concatenateX(double[] X, int count) {
        int len = X.length;
        double[] result = new double[len + len * count];
        for(int index = 0; index < len; index++) {
            result[index] = X[index];
        }
        return result;
    }

    public static double[] computeInputReactionInitX(Map<Integer, List<EMU>> inputReactionSizeEMUsMap) {
        return computeSizeEMUsX(inputReactionSizeEMUsMap);
    }


    public static double[] computeInterReactionInitX(Map<Integer, List<EMU>> interReactionSizeEMUsMap) {
        return computeSizeEMUsX(interReactionSizeEMUsMap);
    }

    public static int computeXLength(Map<Integer, List<EMU>> sizeEMUsMap) {
        int len = 0;
        for(int size : sizeEMUsMap.keySet()) {
            List<EMU> emuList = sizeEMUsMap.get(size);
            for(EMU emu : emuList) {
                len += emu.getSize() + 1;
            }
        }
        return len;
    }

    public static int computeXLengthByEMU(Map<String, EMU> allInterReactionEMUsMap) {
        int len = 0;
        for(String key : allInterReactionEMUsMap.keySet()) {
            EMU emu = allInterReactionEMUsMap.get(key);
            len += emu.getSize() + 1;
        }
        return len;
    }

    public static double[] computeSizeEMUsX(Map<Integer, List<EMU>> sizeEMUsMap) {
        int xLength = computeXLength(sizeEMUsMap);
        double[] result = new double[xLength];
        int base = 0;
        int maxSize = EMUTool.computeMaxSize(sizeEMUsMap);
        for(int size = 1; size <= maxSize; size++) {
            if(sizeEMUsMap.containsKey(size)) {
                List<EMU> emuList = sizeEMUsMap.get(size);
                for(EMU emu : emuList) {
                    for(int vIndex = 0; vIndex <= emu.getSize(); vIndex++) {
                        result[base + vIndex] = emu.getMassIsotopomer(vIndex).getFraction();
                    }
                    base += emu.getSize() + 1;
                }
            }
        }
        return result;
    }

    public static Map<String, double[]> parseSizeEMUKeyXWithDxdw(Map<Integer, List<EMU>> interReactionSizeEMUsMap, double[] Xn) {
        Map<String, double[]> result = new HashMap<>();
        int xLength = computeXLength(interReactionSizeEMUsMap);
        int base = 0;
        int maxSize = EMUTool.computeMaxSize(interReactionSizeEMUsMap);
        for(int size = 1; size <= maxSize; size++) {
            if(interReactionSizeEMUsMap.containsKey(size)) {
                List<EMU> emuList = interReactionSizeEMUsMap.get(size);
                for(EMU emu : emuList) {
                    List<Double> valueXList = new ArrayList<>();
                    for(int vIndex = 0; vIndex <= emu.getSize(); vIndex++) {
                        valueXList.add(Xn[base + vIndex]);
                    }
                    List<Double> valueDxdvList = new ArrayList<>();
                    for(int k = 0; k < Count.freeCount; k++) {
                        int dxdvBase = (k + 1) * xLength + base;
                        for(int vIndex = 0; vIndex <= emu.getSize(); vIndex++) {
                            valueDxdvList.add(Xn[dxdvBase + vIndex]);
                        }
                    }
                    List<Double> valueDxdcList = new ArrayList<>();
                    for(int k = 0; k < Count.cCount; k++) {
                        int dxdcBase = (k + 1 + Count.freeCount) * xLength + base;
                        for(int vIndex = 0; vIndex <= emu.getSize(); vIndex++) {
                            valueDxdcList.add(Xn[dxdcBase + vIndex]);
                        }
                    }
                    double[] x = VectorTool.convertListToVector(valueXList);
                    double[] dxdv = VectorTool.convertListToVector(valueDxdvList);
                    double[] dxdc = VectorTool.convertListToVector(valueDxdcList);
                    x = VectorTool.appendVector(dxdv, x);
                    x = VectorTool.appendVector(dxdc, x);
                    result.put(emu.getKey(), x);
                    base += emu.getSize() + 1;
                }
            }
        }
        return result;
    }
    public static Map<String, double[]> parseSizeEMUKeyX(Map<Integer, List<EMU>> interReactionSizeEMUsMap, double[] Xn) {
        Map<String, double[]> result = new HashMap<>();
        int base = 0;
        int maxSize = EMUTool.computeMaxSize(interReactionSizeEMUsMap);
        for(int size = 1; size <= maxSize; size++) {
            if(interReactionSizeEMUsMap.containsKey(size)) {
                List<EMU> emuList = interReactionSizeEMUsMap.get(size);
                for(EMU emu : emuList) {
                    List<Double> valueList = new ArrayList<>();
                    for(int vIndex = 0; vIndex <= emu.getSize(); vIndex++) {
                        valueList.add(Xn[base + vIndex]);
                    }
                    result.put(emu.getKey(), VectorTool.convertListToVector(valueList));
                    base += emu.getSize() + 1;
                }
            }
        }
        return result;
    }

    public static double[] formatX(double[] x) {
        double[] result = new double[x.length];
        for(int index = 0; index < result.length; index++) {
            result[index] = Util.formatNumber(x[index]);
        }
        return result;
    }

}
