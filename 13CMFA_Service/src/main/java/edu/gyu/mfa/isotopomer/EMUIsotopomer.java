package edu.gyu.mfa.isotopomer;

public class EMUIsotopomer extends AExIsotopomer implements Comparable<EMUIsotopomer>{

    private String code;

    public EMUIsotopomer(){}

    public EMUIsotopomer(String name, String cPosition, String code) {
        this.name = name;
        this.cPosition = cPosition;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public int compareTo(EMUIsotopomer emuIsotopomer) {
        return code.compareTo(emuIsotopomer.getCode());
    }

}
