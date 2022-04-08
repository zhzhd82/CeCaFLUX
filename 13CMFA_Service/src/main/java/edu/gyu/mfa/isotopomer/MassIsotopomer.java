package edu.gyu.mfa.isotopomer;

import edu.gyu.mfa.info.Constant;

import java.util.Objects;

public class MassIsotopomer extends AExIsotopomer implements Comparable<MassIsotopomer>{

    private int mass;

    public MassIsotopomer(String name, String cPosition, int mass) {
        this.name = name;
        this.cPosition = cPosition;
        this.mass = mass;
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public String getKey() {
        return name + Constant.NAME_SPLITTER + cPosition + Constant.NAME_SPLITTER + mass;
    }

    @Override
    public String toString() {
        return "MassIsotopomer{" +
                "name='" + name + '\'' +
                ", cPosition='" + cPosition + '\'' +
                ", mass=" + mass +
                ", fraction=" + fraction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MassIsotopomer that = (MassIsotopomer) o;
        return mass == that.mass &&
                Objects.equals(name, that.name) &&
                Objects.equals(cPosition, that.cPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cPosition, mass);
    }

    @Override
    public int compareTo(MassIsotopomer massIsotopomer) {
        int result;
        if(!name.equals(massIsotopomer.getName())) {
            result = name.compareTo(massIsotopomer.getName());
        } else if(mass != massIsotopomer.getMass()) {
            result = mass - massIsotopomer.getMass();
        } else {
            String[] srcCPositionSplits = cPosition.split(Constant.NAME_SPLITTER);
            String[] targetCPositionSplits = massIsotopomer.getcPosition().split(Constant.NAME_SPLITTER);
            if(srcCPositionSplits.length != targetCPositionSplits.length) {
                result = srcCPositionSplits.length - targetCPositionSplits.length;
            } else {
                result = cPosition.compareTo(massIsotopomer.getcPosition());
            }
        }
        return result;
    }

}
