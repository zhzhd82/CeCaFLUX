package edu.gyu.mfa.emu;

import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.isotopomer.EMUIsotopomer;
import edu.gyu.mfa.isotopomer.MassIsotopomer;
import edu.gyu.mfa.matrix.MatrixTool;
import edu.gyu.mfa.util.Util;
import java.util.*;

public class EMU implements Comparable<EMU> {
    //compound name
    private String name;
    private String cPosition;
    private Map<Integer, MassIsotopomer> massIsotopomerMap;
    private int size;
    private double[][] B;
    private List<EMUIsotopomer> emuIsotopomerList;
    private double[] massIsotopomerVector;
    private String key;

    public EMU() {
        massIsotopomerMap = new HashMap<>();
        emuIsotopomerList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getcPosition() {
        return cPosition;
    }

    public void setcPosition(String cPosition) {
        this.cPosition = cPosition;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void  generateKey() {
        key = name + Constant.NAME_SPLITTER + cPosition + Constant.NAME_SPLITTER + size;
    }

    public void generateMassIsotopomer() {
        for(int weight = 0; weight <= size; weight++) {
            massIsotopomerMap.put(weight, new MassIsotopomer(name, cPosition, weight));
        }
    }

    public void generateEMUIsotopomer() {
        if(emuIsotopomerList.size() > 0) {
            return;
        }
        for (int value = 0; value <= (int) Math.pow(2, size) - 1; value++) {
            String code = Util.paddingPrefixWithZero(Long.toBinaryString(value), size);
            emuIsotopomerList.add(new EMUIsotopomer(name, cPosition, code));
        }
        Collections.sort(emuIsotopomerList);
    }

    public MassIsotopomer getMassIsotopomer(int mass) {
        return massIsotopomerMap.get(mass);
    }

    public String getKey() {
        return key;
    }

    public double[][] getB() {
        return B;
    }

    public void setB(double[][] b) {
        B = b;
    }

    public Map<Integer, MassIsotopomer> getMassIsotopomerMap() {
        return massIsotopomerMap;
    }

    public void computeB() {
        if(B != null) {
            return;
        }
        setB(MatrixTool.computeB(size));
    }

    public void generateMassIsotopomerVector() {
        massIsotopomerVector = new double[size+1];
        for(int index = 0; index <= size; index++) {
            massIsotopomerVector[index] = massIsotopomerMap.get(index).getFraction();
        }
    }

    public double[] getMassIsotopomerVector() {
        return massIsotopomerVector;
    }

    public void setMassIsotopomerVectorValue(int index, double value) {
        massIsotopomerVector[index] = value;
    }

    public List<EMUIsotopomer> getEmuIsotopomerList() {
        return emuIsotopomerList;
    }

    @Override
    public String toString() {
        return "EMU{" + key + '}';
    }

    @Override
    public int compareTo(EMU o) {
        int result;
        if(!name.equals(o.getName())) {
            result = name.compareTo(o.getName());
        } else if(size != o.getSize()) {
            result = size - o.getSize();
        } else {
            String[] srcCPositionSplits = cPosition.split(Constant.NAME_SPLITTER);
            String[] targetCPositionSplits = o.getcPosition().split(Constant.NAME_SPLITTER);
            if(srcCPositionSplits.length != targetCPositionSplits.length) {
                result = srcCPositionSplits.length - targetCPositionSplits.length;
            } else {
                result = cPosition.compareTo(o.getcPosition());
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EMU emu = (EMU) o;
        return Objects.equals(key, emu.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
