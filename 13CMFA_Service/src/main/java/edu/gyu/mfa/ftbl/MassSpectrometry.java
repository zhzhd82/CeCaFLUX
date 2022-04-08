package edu.gyu.mfa.ftbl;

import edu.gyu.mfa.info.Constant;

public class MassSpectrometry {
    private String meta_name;
    private String fragment;
    private int weight;
    private int size;

    public MassSpectrometry(String meta_name, String fragment, int weight) {
        this.meta_name = meta_name;
        this.fragment = fragment;
        this.weight = weight;
        size = fragment.split(Constant.NAME_SPLITTER).length;
    }

    public int getWeight() {
        return weight;
    }

    public String getEMUKey() {
        return meta_name + Constant.NAME_SPLITTER + fragment + Constant.NAME_SPLITTER + size;
    }

    public int getSize() {
        return size;
    }

    public String getMeta_name() {
        return meta_name;
    }

    public String getFragment() {
        return fragment;
    }

    public String getWeightKey() {
        return meta_name + Constant.NAME_SPLITTER + fragment + Constant.NAME_SPLITTER + weight;
    }

}
