package edu.gyu.mfa.reaction;

import edu.gyu.mfa.calculator.SizeBaseCalculator;
import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.matrix.EMUCompositionCoefficientMatrixElement;

import java.util.*;

public class ReactionCoefficientMatrixElementTool {

    public static ReactionCoefficientMatrixElement computeReactionCoefficientMatrixElement(List<EMUReaction> emuReactions,
                                                                                           Map<String, EMU> allInterReactionEMUsMap,
                                                                                           SizeBaseCalculator sbCalculator) {
        ReactionCoefficientMatrixElement reactionCoefficientMatrixElement = new ReactionCoefficientMatrixElement();
        EMUCompositionCoefficientMatrixElement emuCompositionCoefficientMatrixElement = new EMUCompositionCoefficientMatrixElement();
        reactionCoefficientMatrixElement.setEmuCompositionCoefficientMatrixElement(emuCompositionCoefficientMatrixElement);

        Map<String, Flux> reactionNameFluxMap = new HashMap<>();

        for(String emuKey : allInterReactionEMUsMap.keySet()) {
            EMU emu = allInterReactionEMUsMap.get(emuKey);
            int tBase = sbCalculator.findInterReactionEMUBase(emu);
            Map<String, List<Flux>> emuReactionFluxListMap = new HashMap<>();
            for(EMUReaction emuReaction : emuReactions) {
                String rName = emuReaction.getName();
                Flux flux;
                if(reactionNameFluxMap.containsKey(rName)) {
                    flux = reactionNameFluxMap.get(rName);
                } else {
                    flux = emuReaction.getFlux().clone();
                    reactionNameFluxMap.put(rName, flux);
                }

                if(isReactionReactantEMU(emuReaction, emu)) {
                    List<Flux> curEMUList = new ArrayList<>();
                    curEMUList.add(flux);
                    if(isReactionReactantEMUsSame(emuReaction)) {
                        curEMUList.add(flux);
                    }
                    List<Flux> srcEMUList = emuReactionFluxListMap.get(rName);
                    if(srcEMUList == null || srcEMUList.size() < curEMUList.size()) {
                        emuReactionFluxListMap.put(rName, curEMUList);
                    }
                } else if(isReactionProductEMU(emuReaction, emu)){
                    if(ReactionTool.isInputReaction(emuReaction)) {
                        EMU inputEMU = emuReaction.getReactant(0);
                        int inputBase = sbCalculator.findInputReactionEMUBase(inputEMU);
                        reactionCoefficientMatrixElement.addInputReactionEMUElement(
                                flux, inputBase, inputEMU.getSize(), tBase, rName);
                    } else if(ReactionTool.isSingleInterReaction(emuReaction)
                                && ReactionTool.isOneProductReaction(emuReaction)) {
                        EMU rEMU = emuReaction.getReactant(0);
                        if(isEMUExists(allInterReactionEMUsMap, rEMU)) {
                            int mBase = sbCalculator.findInterReactionEMUBase(rEMU);
                            reactionCoefficientMatrixElement.addSingleInterReactionEMUElement(
                                    flux, mBase, rEMU.getSize(), tBase, rName);
                        }
                    } else if(ReactionTool.isMultiInterReaction(emuReaction)
                                && ReactionTool.isOneProductReaction(emuReaction)) {
                        EMU r1EMU = emuReaction.getReactant(0);
                        EMU r2EMU = emuReaction.getReactant(1);
                        if(isEMUExists(allInterReactionEMUsMap, r1EMU, r2EMU)) {
                            int mBase1 = sbCalculator.findInterReactionEMUBase(r1EMU);
                            int mBase2 = sbCalculator.findInterReactionEMUBase(r2EMU);
                            emuCompositionCoefficientMatrixElement.addEMU1Element(flux, mBase1, r1EMU.getSize(), tBase, rName);
                            emuCompositionCoefficientMatrixElement.addEMU2Element(flux, mBase2, r2EMU.getSize(), tBase, rName);
                        }
                    }
                }
            }
            for(String rName : emuReactionFluxListMap.keySet()) {
                for(Flux flux : emuReactionFluxListMap.get(rName)) {
                    reactionCoefficientMatrixElement.addConsumptionReactionEMUElement(flux, tBase, emu.getSize(), tBase, rName);
                }
            }
        }
        return reactionCoefficientMatrixElement;
    }

    private static boolean isReactionProductEMU(EMUReaction emuReaction, EMU emu) {
        boolean result = false;
        for(EMU pEMU : emuReaction.getProducts()) {
            if(emu.getKey().equals(pEMU.getKey())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean isReactionReactantEMU(EMUReaction emuReaction, EMU emu) {
        boolean result = false;
        for(EMU rEMU : emuReaction.getReactants()) {
            if(emu.getKey().equals(rEMU.getKey())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean isReactionReactantEMUsSame(EMUReaction emuReaction) {
        boolean result = true;
        List<EMU> reactantList = emuReaction.getReactants();
        if(reactantList.size() == 1) {
            result = false;
        } else {
            String emuKey = reactantList.get(0).getKey();
            for(int index = 1; index < reactantList.size(); index++) {
                EMU rEMU = reactantList.get(index);
                if(!rEMU.getKey().equals(emuKey)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    private static boolean isEMUExists(Map<String, EMU> allInterReactionEMUsMap, EMU... emus) {
        return isEMUExists(allInterReactionEMUsMap, Arrays.asList(emus));
    }

    private static boolean isEMUExists(Map<String, EMU> allInterReactionEMUsMap, List<EMU> emus) {
        boolean result = true;
        for(EMU emu : emus) {
            if(!allInterReactionEMUsMap.containsKey(emu.getKey())) {
                result = false;
                break;
            }
        }
        return result;
    }

}
