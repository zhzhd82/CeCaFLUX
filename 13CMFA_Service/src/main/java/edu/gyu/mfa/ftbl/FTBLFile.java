package edu.gyu.mfa.ftbl;

import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.isotopomer.Isotopomer;
import java.util.List;
import java.util.Map;

public class FTBLFile {

    private List<MassSpectrometry> massSpectrometryList;
    private List<Equality> equalityList;
    private List<InEquality> inEqualityList;
    private List<FluxNet> fluxNetList;
    private Map<String, List<Isotopomer>> compIsotopomersMap;
    private Map<String, Double> poolSizeMap;

    private IFTBLFileParser fileParser;

    public FTBLFile(ModelingCase modelingCase) {
        fileParser = new FTBLFileParserImpl(modelingCase);
        massSpectrometryList = fileParser.parseMassSpectrometry();
        equalityList = fileParser.parseEquality();
        inEqualityList = fileParser.parseInEquality();
        fluxNetList = fileParser.parseFluxNet();
        compIsotopomersMap = fileParser.parseCompoundIsotopomer();
        poolSizeMap = fileParser.parsePoolSize();
    }

    public List<MassSpectrometry> getMassSpectrometryList() {
        return massSpectrometryList;
    }

    public List<Equality> getEqualityList() {
        return equalityList;
    }

    public List<InEquality> getInequalityList() {
        return inEqualityList;
    }

    public List<FluxNet> getFluxNetList() {
        return fluxNetList;
    }

    public Map<String, List<Isotopomer>> getCompIsotopomersMap() {
        return compIsotopomersMap;
    }

    public Map<String, Double> getPoolSizeMap() {
        return poolSizeMap;
    }

    public Map<String, Map<Double, MassSpectrometryTimePointValue>> getWeightKeyTimePointValueMap() {
        return fileParser.getWeightKeyTimePointValueMap();
    }

}
