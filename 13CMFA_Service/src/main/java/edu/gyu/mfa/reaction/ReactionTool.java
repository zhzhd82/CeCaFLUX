package edu.gyu.mfa.reaction;

import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.emu.EMUTool;
import edu.gyu.mfa.ftbl.FTBLFile;
import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.info.Norm2ValueFluxX;
import edu.gyu.mfa.isotopomer.Isotopomer;
import edu.gyu.mfa.util.SampleUtil;
import edu.gyu.mfa.util.Util;

import java.util.*;

public class ReactionTool {

    public static List<EMUReaction> generateEMUReactions(List<Reaction> reactions) {
        List<EMUReaction> results = new ArrayList<>();
        for (Reaction reaction : reactions) {
            for(Compound product : reaction.getProducts()) {
                boolean single = false;
                for(Compound reactant : reaction.getReactants()) {
                    if(Util.contains(product.getCarbon(), reactant.getCarbon(), Constant.CARBON_SPLITTER)) {
                        single = true;
                        results.addAll(EMUTool.generateEMUReactionFromReactants(reaction,product,reactant));
                        break;
                    }
                }
                if(single) {
                    continue;
                }
                results.addAll(EMUTool.generateEMUReactionFromReactants(reaction, product,reaction.getReactants()));
            }
            if(reaction.getProducts().size() > 1) {
                List<EMUReaction> tmp = EMUTool.generateTwoProductsEMUReaction(reaction);
                if(tmp.size() > 0) {
                    results.addAll(tmp);
                }
            }
        }
        return results;
    }

    public static void computeEMUReactionFluxFromCompoundReaction(List<Reaction> reactions, List<EMUReaction> emuReactions) {
        Map<String, Flux> nameFluxMap = new HashMap<>();
        for(Reaction reaction : reactions) {
            nameFluxMap.put(reaction.getName(), reaction.getFlux());
        }
        for(EMUReaction emuReaction : emuReactions) {
            emuReaction.setFlux(nameFluxMap.get(emuReaction.getName()));
        }
    }

    public static void computeInitIsotopomerValue(FTBLFile ftblFile, List<Reaction> reactions) {
        Map<String, List<Isotopomer>> isotopomersMap = ftblFile.getCompIsotopomersMap();
        for (Reaction reaction : reactions) {
            List<Compound> compounds = new ArrayList<>();
            compounds.addAll(reaction.getReactants());
            compounds.addAll(reaction.getProducts());
            for (Compound compound : compounds) {
                if (isotopomersMap.containsKey(compound.getName())) {
                    List<Isotopomer> isotopomers = isotopomersMap.get(compound.getName());
                    Collections.sort(isotopomers);
                    for (Isotopomer isotopomer : compound.getIsotopomers()) {
                        for (Isotopomer isotopomerInit : isotopomers) {
                            if (isotopomer.getCode().equals(isotopomerInit.getCode())) {
                                isotopomer.setValue(isotopomerInit.getValue());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void setReactionFlux(List<Reaction> reactions, String reactionFLux) {
        Map<String, Double> numberSamplingReactionFluxMap = new HashMap<>();
        String result = reactionFLux.substring(reactionFLux.indexOf("(") + 1, reactionFLux.length() - 1);
        String[] resultSplits = result.split(",");
        for(String split : resultSplits) {
            String[] nameValueSplits = split.split("=");
            numberSamplingReactionFluxMap.put(nameValueSplits[0].trim(), Double.parseDouble(nameValueSplits[1].trim()));
        }
        for(Reaction reaction : reactions) {
            reaction.setFlux(numberSamplingReactionFluxMap.get(reaction.getName()));
        }
    }

    public static boolean isInputReaction(EMUReaction emuReaction) {
        return emuReaction.getType().contains(Constant.INPUT_REACTION);
    }

    public static boolean isOutputReaction(EMUReaction emuReaction) {
        return emuReaction.getType().contains(Constant.OUTPUT_REACTION);
    }

    public static boolean isInterReaction(EMUReaction emuReaction) {
        return emuReaction.getType().contains(Constant.INTER_REACTION);
    }

    public static boolean isSingleInterReaction(EMUReaction emuReaction) {
        return emuReaction.getType().contains(Constant.INTER_REACTION) &&
                emuReaction.getType().contains(Constant.SINGLE_REACTION);
    }

    public static boolean isOneProductReaction(EMUReaction emuReaction) {
        return emuReaction.getProducts().size() == 1;
    }

    public static boolean isMultiInterReaction(EMUReaction emuReaction) {
        return emuReaction.getType().contains(Constant.INTER_REACTION) &&
                emuReaction.getType().contains(Constant.MULTI_REACTION);
    }

    public static List<Norm2ValueFluxX> sampleReactionFluxAndConvertToNorm2ValueFluxX(List<Reaction> reactions, FTBLFile ftblFile, int sampleCount) {
        List<Norm2ValueFluxX> result = new ArrayList<>();
        Argument.sample_count = sampleCount;
        List<String> reactionFluxesList = SampleUtil.sampleReactionFluxWithFile(reactions, ftblFile, Argument.python, null).get(0);
        for(String reactionFluxString : reactionFluxesList) {
            ReactionTool.setReactionFlux(reactions, reactionFluxString);
            double[] vFreeValues = new double[Count.freeCount];
            int freeIndex=0;
            for(Reaction reaction : reactions) {
                if(!reaction.getFlux().isConsed()) {
                    vFreeValues[freeIndex++] = reaction.getFlux().getValue();
                }
            }
            Norm2ValueFluxX norm2ValueFluxX = new Norm2ValueFluxX();
            norm2ValueFluxX.setvFreeValues(vFreeValues);
            result.add(norm2ValueFluxX);
        }
        return result;
    }

    public static List<Norm2ValueFluxX> sampleReactionFluxWithOptimizerAndConvertToNorm2ValueFluxX(List<Reaction> reactions, FTBLFile ftblFile) {
        List<Norm2ValueFluxX> result = new ArrayList<>();
        List<List<String>> reactionFluxesList = SampleUtil.sampleReactionFluxWithOptimizer(reactions, ftblFile, Argument.python, null);
        for(List<String> reactionFluxList : reactionFluxesList) {
            Map<String, Double> reactionNameFluxMap = new HashMap<>();
            for(String reactionFluxString : reactionFluxList) {
                String[] nameFluxSplits = reactionFluxString.split("\\s:\\s");
                reactionNameFluxMap.put(nameFluxSplits[0].trim(), Double.parseDouble(nameFluxSplits[1].trim()));
            }
            double[] vFreeValues = new double[Count.freeCount];
            int freeIndex=0;
            for(Reaction reaction : reactions) {
                if(!reaction.getFlux().isConsed()) {
                    vFreeValues[freeIndex++] = reactionNameFluxMap.get(reaction.getName());
                }
            }
            Norm2ValueFluxX norm2ValueFluxX = new Norm2ValueFluxX();
            norm2ValueFluxX.setvFreeValues(vFreeValues);
            result.add(norm2ValueFluxX);
        }
        return result;
    }

    public static double[] parseVFreeValuesFromReactionFluxString(String reactionFluxString, List<Reaction> reactions) {
        setReactionFlux(reactions, reactionFluxString);
        double[] vFreeValues = new double[Count.freeCount];
        int freeIndex = 0;
        for(Reaction reaction : reactions) {
            if(!reaction.getFlux().isConsed()) {
                vFreeValues[freeIndex++] = reaction.getFlux().getValue();
            }
        }
        return vFreeValues;
    }

    public static String convertReactionFluxToString(List<Reaction> reactions) {
        StringBuffer fluxSB = new StringBuffer();
        for(int index = 0; index < reactions.size(); index++) {
            Reaction reaction = reactions.get(index);
            fluxSB.append(reaction.getName() + Constant.INNER_SPLITTER + Util.formatNumber(reaction.getFlux().getValue()));
            if(index < reactions.size() - 1) {
                fluxSB.append(Constant.OUTER_SPLITTER);
            }
        }
        return fluxSB.toString();
    }

}
