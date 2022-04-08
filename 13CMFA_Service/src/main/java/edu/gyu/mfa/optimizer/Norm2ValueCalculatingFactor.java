package edu.gyu.mfa.optimizer;

import edu.gyu.mfa.x.EMUX;

import java.util.Map;

public class Norm2ValueCalculatingFactor {

    private EMUX emux;
    private Map<Integer, Double> C_PosValueMap;
    private Map<Integer, Double> CLCJ_PosValueMap;

    public Norm2ValueCalculatingFactor() {}

    public Norm2ValueCalculatingFactor(EMUX emux, Map<Integer, Double> C_PosValueMap) {
        this.emux = emux;
        this.C_PosValueMap = C_PosValueMap;
    }

    public Norm2ValueCalculatingFactor(EMUX emux, Map<Integer, Double> C_PosValueMap, Map<Integer, Double> CLCJ_PosValueMap) {
        this.emux = emux;
        this.C_PosValueMap = C_PosValueMap;
        this.CLCJ_PosValueMap = CLCJ_PosValueMap;
    }

    public EMUX getEmux() {
        return emux;
    }

    public void setEmux(EMUX emux) {
        this.emux = emux;
    }

    public Map<Integer, Double> getC_PosValueMap() {
        return C_PosValueMap;
    }

    public void setC_PosValueMap(Map<Integer, Double> c_PosValueMap) {
        C_PosValueMap = c_PosValueMap;
    }

    public Map<Integer, Double> getCLCJ_PosValueMap() {
        return CLCJ_PosValueMap;
    }

    public void setCLCJ_PosValueMap(Map<Integer, Double> CLCJ_PosValueMap) {
        this.CLCJ_PosValueMap = CLCJ_PosValueMap;
    }
}
