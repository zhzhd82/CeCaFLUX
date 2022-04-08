package edu.gyu.mfa.rungekutta;

import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.util.Util;
import edu.gyu.mfa.x.XTool;
import flanagan.integration.DerivnFunction;

import java.util.List;
import java.util.Map;

public class FlanaganDerivn extends AbstractDerivn implements DerivnFunction {

    private List<Double> hTimePointList;
    public Map<Integer, List<EMU>> interReactionSizeEMUsMap;

    public FlanaganDerivn(BaseDerivn _baseDerivn) {
        super(_baseDerivn);
    }

    @Override
    public double[] derivn(double t, double[] x) {
        double[] interX = XTool.formatX(x);
        double formattedT = Util.formatNumber(t);
        hTimePointList.add(formattedT);
        if(Argument.isAdaptive && hTimePointList.size() > 500000) {
            throw new RuntimeException("too much time points");
        }
        return super.baseDerivn(formattedT, interX);
    }

    public void sethTimePointList(List<Double> hTimePointList) {
        this.hTimePointList = hTimePointList;
    }

    public void setInterReactionSizeEMUsMap(Map<Integer, List<EMU>> interReactionSizeEMUsMap) {
        this.interReactionSizeEMUsMap = interReactionSizeEMUsMap;
    }
}
