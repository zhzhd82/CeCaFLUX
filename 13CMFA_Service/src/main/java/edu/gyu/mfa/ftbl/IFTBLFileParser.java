package edu.gyu.mfa.ftbl;

import edu.gyu.mfa.isotopomer.Isotopomer;

import java.util.List;
import java.util.Map;

public interface IFTBLFileParser {

    List<FluxNet> parseFluxNet();
    List<Equality> parseEquality();
    List<InEquality> parseInEquality();
    Map<String, List<Isotopomer>> parseCompoundIsotopomer();
    List<MassSpectrometry> parseMassSpectrometry();
    Map<String, Double> parsePoolSize();
    Map<String,Map<Double, MassSpectrometryTimePointValue>> getWeightKeyTimePointValueMap();

}
