package edu.gyu.mfa.ftbl;

import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.isotopomer.Isotopomer;
import edu.gyu.mfa.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTBLFileParserImpl implements IFTBLFileParser{

    private ModelingCase modelingCase;
    private Map<String,Map<Double, MassSpectrometryTimePointValue>> weightKeyTimePointValueMap;

    public FTBLFileParserImpl(ModelingCase modelingCase) {
        this.modelingCase = modelingCase;
    }

    @Override
    public List<FluxNet> parseFluxNet() {
        List<FluxNet> result = new ArrayList<>();
        String[] fluxNetSplits = modelingCase.getFluxes_net().split(Constant.OUTER_SPLITTER);
        for(String fluxNet : fluxNetSplits) {
            if(Util.isBlank(fluxNet)) {
                continue;
            }
            String[] fluxNetParts = fluxNet.split(Constant.INNER_SPLITTER);
            result.add(new FluxNet(fluxNetParts[0].trim(), Double.parseDouble(fluxNetParts[1].trim())));
        }
        return result;
    }

    @Override
    public Map<String, Double> parsePoolSize() {
        Map<String, Double> result = new HashMap<>();
        String[] poolSizeSplits = modelingCase.getPool_size().split(Constant.OUTER_SPLITTER);
        for(String poolSizeSplit : poolSizeSplits) {
            if(Util.isBlank(poolSizeSplit)) {
                continue;
            }
            String[] poolSize = poolSizeSplit.trim().split(Constant.INNER_SPLITTER);
            result.put(poolSize[0].trim(), Double.parseDouble(poolSize[1].trim()));
        }
        return result;
    }

    @Override
    public Map<String, Map<Double, MassSpectrometryTimePointValue>> getWeightKeyTimePointValueMap() {
        return weightKeyTimePointValueMap;
    }

    @Override
    public List<Equality> parseEquality() {
        List<Equality> results = new ArrayList<>();
        String[] equalitiesSplits = modelingCase.getEqualities().split(Constant.OUTER_SPLITTER);
        for(String equalities : equalitiesSplits) {
            if(Util.isBlank(equalities)) {
                continue;
            }
            String[] equalitiesParts = equalities.split(Constant.INNER_SPLITTER);
            Equality equality = new Equality();
            equality.setValue(Double.parseDouble(equalitiesParts[2].trim()));
            String[] coefficientNameSplits = equalitiesParts[0].split("\\*");
            equality.addNameCoefficient(coefficientNameSplits[1].trim(), Double.parseDouble(coefficientNameSplits[0].trim()));
            coefficientNameSplits = equalitiesParts[1].split("\\*");
            equality.addNameCoefficient(coefficientNameSplits[1].trim(), Double.parseDouble(coefficientNameSplits[0].trim()));
            results.add(equality);
        }

        return results;
    }

    @Override
    public List<InEquality> parseInEquality() {
        List<InEquality> result = new ArrayList<>();

        String[] inEqualitiesSplits = modelingCase.getInequalities().split(Constant.OUTER_SPLITTER);
        for(String inEqualities : inEqualitiesSplits) {
            if(Util.isBlank(inEqualities)) {
                continue;
            }

            if(inEqualities.contains(">=")) {
                String[] inEqualitiesParts = inEqualities.split(">=");
                result.add(new InEquality(inEqualitiesParts[0].trim(), inEqualitiesParts[1].trim(), "None"));
            } else if(inEqualities.contains(">")) {
                String[] inEqualitiesParts = inEqualities.split(">");
                result.add(new InEquality(inEqualitiesParts[0].trim(), inEqualitiesParts[1].trim(), "None"));
            } else if(inEqualities.contains("<=")) {
                String[] inEqualitiesParts = inEqualities.split("<=");
                result.add(new InEquality(inEqualitiesParts[0].trim(), "None", inEqualitiesParts[1].trim()));
            } else if(inEqualities.contains("<")) {
                String[] inEqualitiesParts = inEqualities.split("<");
                result.add(new InEquality(inEqualitiesParts[0].trim(), "None", inEqualitiesParts[1].trim()));
            }
        }

        return result;
    }

    @Override
    public Map<String, List<Isotopomer>> parseCompoundIsotopomer() {
        Map<String, List<Isotopomer>> isotopomersMap = new HashMap<>();
        String[] labelInputSplits = modelingCase.getLabel_input().split(Constant.OUTER_SPLITTER);
        String name = "";
        List<Isotopomer> tmp = null;
        for(String labelInput : labelInputSplits) {
            if(Util.isBlank(labelInput)) {
                continue;
            }
            String[] labelInputParts = labelInput.split(Constant.INNER_SPLITTER);
            String tmpName = labelInputParts[0].trim();
            if(!tmpName.equals(name)) {
                name = tmpName;
                tmp = new ArrayList<>();
                isotopomersMap.put(name, tmp);
            }
            String code = labelInputParts[1].trim();
            double value = Double.parseDouble(labelInputParts[2].trim());
            tmp.add(new Isotopomer(name, code, value));
        }
        return isotopomersMap;
    }

    @Override
    public List<MassSpectrometry> parseMassSpectrometry() {
        List<MassSpectrometry> result = new ArrayList<>();
        weightKeyTimePointValueMap = new HashMap<>();
        Map<Double,MassSpectrometryTimePointValue> timePointValueMap;
        Map<String, Integer> checkMap = new HashMap<>();
        String[] massSpectrometrySplits = modelingCase.getMass_spectrometry().split(Constant.OUTER_SPLITTER);
        for(String massSpectrometry : massSpectrometrySplits) {
            String[] massSepectrometryParts = massSpectrometry.split(Constant.INNER_SPLITTER);
            String compName = massSepectrometryParts[0].trim();
            String fragment = massSepectrometryParts[1].trim();
            if(fragment.contains(",")) {
                fragment = fragment.replaceAll(",",Constant.NAME_SPLITTER);
            }
            int weight = Integer.parseInt(massSepectrometryParts[2].trim());
            double value = Double.parseDouble(massSepectrometryParts[3].trim());
            double deviation = Double.parseDouble(massSepectrometryParts[4].trim());
            double timePoint = Double.parseDouble(massSepectrometryParts[5].trim());
            MassSpectrometry massSpectrometryObj = new MassSpectrometry(compName, fragment, weight);
            String weightKey = massSpectrometryObj.getWeightKey();
            if(!checkMap.containsKey(weightKey)) {
                result.add(massSpectrometryObj);
                checkMap.put(weightKey, 1);
            }
            timePointValueMap = weightKeyTimePointValueMap.get(weightKey);
            if(timePointValueMap == null) {
                timePointValueMap = new HashMap<>();
                weightKeyTimePointValueMap.put(weightKey, timePointValueMap);
            }
            timePointValueMap.put(timePoint, new MassSpectrometryTimePointValue(weightKey, value, deviation, timePoint));
        }
        return result;
    }

}
