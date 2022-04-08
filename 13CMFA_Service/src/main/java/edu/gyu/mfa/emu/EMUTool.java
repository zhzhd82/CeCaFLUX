package edu.gyu.mfa.emu;

import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.isotopomer.EMUIsotopomer;
import edu.gyu.mfa.isotopomer.Isotopomer;
import edu.gyu.mfa.isotopomer.MassIsotopomer;
import edu.gyu.mfa.matrix.MatrixTool;
import edu.gyu.mfa.matrix.VectorTool;
import edu.gyu.mfa.reaction.EMUReaction;
import edu.gyu.mfa.reaction.Reaction;
import edu.gyu.mfa.util.Util;

import java.util.*;

public class EMUTool {

    public static EMU generateEMUFromIsotopomer(Isotopomer isotopomer) {
        EMU emu = new EMU();
        emu.setName(isotopomer.getName());
        int size = 0;
        StringBuffer positionSB = new StringBuffer();
        int[] vector = isotopomer.getVector();
        for(int index = 0; index < vector.length; index++) {
            if(vector[index] == 1) {
                size++;
                positionSB.append(index+1);
                positionSB.append(Constant.NAME_SPLITTER);
            }
        }
        if(positionSB.length() > 0) {
            positionSB.deleteCharAt(positionSB.length() - 1);
        }
        emu.setcPosition(positionSB.toString());
        emu.setSize(size);
        emu.generateKey();
        emu.generateMassIsotopomer();
        emu.generateEMUIsotopomer();
        emu.computeB();
        return emu;
    }


    public static EMU generateEMUFromNameAndVector(String name, String carbon, int[] vector) {
        return generateEMUFromIsotopomer(new Isotopomer(name, carbon, vector));
    }

    public static List<EMUReaction> generateEMUReactionFromReactants(Reaction reaction, Compound product, List<Compound> reactantList) {
        Compound[] array = new Compound[reactantList.size()];
        for(int index = 0; index < reactantList.size(); index++) {
            array[index] = reactantList.get(index);
        }
        return generateEMUReactionFromReactants(reaction,product,array);
    }

    public static List<EMUReaction> generateEMUReactionFromReactants(Reaction reaction, Compound product, Compound... reactants) {
        if(reactants.length == 0) {
            return null;
        }
        List<EMUReaction> results = new ArrayList<>();
        List<List<Isotopomer>> isotopomerCombinationList = new ArrayList<>();

        if(reactants.length == 1) {
            for(Isotopomer isotopomer : reactants[0].getIsotopomers()) {
                isotopomerCombinationList.add(Arrays.asList(isotopomer));
            }
        } else {
            for(Isotopomer isotopomer1 : reactants[0].getIsotopomers()) {
                for(Isotopomer isotopomer2 : reactants[1].getIsotopomers()) {
                    isotopomerCombinationList.add(Arrays.asList(isotopomer1, isotopomer2));
                }
            }
        }

        for(List<Isotopomer> isotopomerCombination : isotopomerCombinationList) {
            int[] pVector = null;
            int rWeight = 0;
            EMUReaction emuReaction = new EMUReaction();
            emuReaction.setName(reaction.getName());
            for(Isotopomer isotopomer : isotopomerCombination) {
                rWeight += VectorTool.parseWeightFromVector(isotopomer.getVector());
                int[] mtv = MatrixTool.matrixTimesVector(
                        reaction.findAtomTransferMatrix(isotopomer.getName(), isotopomer.getCarbon(), product.getName(), product.getCarbon())
                        , isotopomer.getVector());
                if(pVector == null) {
                    pVector = mtv;
                } else {
                    pVector = VectorTool.addVector(mtv, pVector);
                }
                EMU reactant = generateEMUFromIsotopomer(isotopomer);
                if(reactant.getSize() > 0) {
                    emuReaction.addReactant(reactant);
                }
            }
            int pWeight = VectorTool.parseWeightFromVector(pVector);
            if(rWeight == 0 || rWeight != pWeight) {
                continue;
            }
            emuReaction.addProduct(generateEMUFromNameAndVector(product.getName(), product.getCarbon(), pVector));
            emuReaction.computeMaxMass();
            emuReaction.parseType(reaction.getType());
            results.add(emuReaction);
        }
        return results;
    }

    public static List<EMUReaction> generateTwoProductsEMUReaction(Reaction reaction) {
        List<EMUReaction> results = new ArrayList<>();

        for(Compound reactant : reaction.getReactants()) {
            for(Isotopomer rIsotopomer : reactant.getIsotopomers()) {
                int rWeight = VectorTool.parseWeightFromVector(rIsotopomer.getVector());
                if(rWeight >= 2) {
                    EMUReaction emuReaction = new EMUReaction();
                    emuReaction.setName(reaction.getName());
                    Compound product1 = reaction.getProduct(0);
                    Compound product2 = reaction.getProduct(1);
                    int[] pVector1 = MatrixTool.matrixTimesVector(
                            reaction.findAtomTransferMatrix(rIsotopomer.getName(), rIsotopomer.getCarbon(),
                                    product1.getName(), product1.getCarbon())
                            , rIsotopomer.getVector());
                    int[] pVector2 = MatrixTool.matrixTimesVector(
                            reaction.findAtomTransferMatrix(rIsotopomer.getName(), rIsotopomer.getCarbon(),
                                    product2.getName(), product2.getCarbon())
                            , rIsotopomer.getVector());
                    int pWeight1 = VectorTool.parseWeightFromVector(pVector1);
                    int pWeight2 = VectorTool.parseWeightFromVector(pVector2);
                    if(pWeight1 == 0 || pWeight2 == 0) {
                        continue;
                    }
                    emuReaction.addReactant(generateEMUFromIsotopomer(rIsotopomer));
                    emuReaction.addProduct(generateEMUFromNameAndVector(product1.getName(), product1.getCarbon(), pVector1));
                    emuReaction.addProduct(generateEMUFromNameAndVector(product2.getName(), product2.getCarbon(), pVector2));
                    emuReaction.parseType(reaction.getType());
                    results.add(emuReaction);
                }
            }
        }
        return results;
    }

    public static boolean checkAndAddEMUToMap(EMU emu, Map<String, Integer> map) {
        if(map.containsKey(emu.getKey())) {
            return true;
        }
        map.put(emu.getKey(), 1);
        return false;
    }

    public static int parseSizeFromEMUKey(String mKey) {
        String[] splits = mKey.split(Constant.NAME_SPLITTER);
        return Integer.parseInt(splits[splits.length - 1]);
    }

    public static int computeMaxSize(Map<Integer, List<EMU>> sizeEMUsMap) {
        int maxSize = 0;
        for(int size : sizeEMUsMap.keySet()) {
            if(size > maxSize) {
                maxSize = size;
            }
        }
        return maxSize;
    }

    public static void computeInterReactionInitValue(Map<String, EMU> emuMap) {
        for(String emuKey : emuMap.keySet()) {
            EMU emu = emuMap.get(emuKey);
            for(int mass = 0; mass <= emu.getSize(); mass++) {
                MassIsotopomer massIsotopomer = emu.getMassIsotopomer(mass);
                int n = emu.getSize();
                double fraction = Util.combination(mass, n) * Math.pow(0.011, mass) *
                        Math.pow(0.989, n - mass);
                massIsotopomer.setFraction(fraction);
            }
            emu.generateMassIsotopomerVector();
        }
    }

    public static void computeInputReactionInitValue(List<Reaction> reactions, List<EMUReaction> emuReactions) {
        Map<String, List<Isotopomer>> nameIsotopomersMap = new HashMap<>();
        for(Reaction reaction : reactions) {
            if(!reaction.getType().contains(Constant.INPUT_REACTION)) {
                continue;
            }
            for(Compound comp : reaction.getReactants()) {
                if(nameIsotopomersMap.containsKey(comp.getName())) {
                    continue;
                }
                nameIsotopomersMap.put(comp.getName(), comp.getIsotopomers());
            }
        }
        for(EMUReaction emuReaction : emuReactions) {
            if(!emuReaction.getType().contains(Constant.INPUT_REACTION)) {
                continue;
            }
            for(EMU emu : emuReaction.getReactants()) {
                List<Isotopomer> isotopomers = nameIsotopomersMap.get(emu.getName());
                List<EMUIsotopomer> emuIsotopomers = emu.getEmuIsotopomerList();
                for(EMUIsotopomer emuIsotopomer : emuIsotopomers) {
                    double fraction = 0;
                    for(Isotopomer isotopomer : isotopomers) {
                        if(Util.containsSubString(emuIsotopomer.getCode(), isotopomer.getCode(), emuIsotopomer.getcPosition())) {
                            fraction += isotopomer.getValue();
                        }
                    }
                    emuIsotopomer.setFraction(fraction);
                }
                double[] vector = new double[emuIsotopomers.size()];
                for(int index = 0; index < emuIsotopomers.size(); index++) {
                    vector[index] = emuIsotopomers.get(index).getFraction();
                }
                double[] mtv = MatrixTool.matrixTimesVector(emu.getB(), vector);
                for(int mass = 0; mass < mtv.length; mass++) {
                    emu.getMassIsotopomer(mass).setFraction(mtv[mass]);
                }
                emu.generateMassIsotopomerVector();
            }
        }
    }

}
