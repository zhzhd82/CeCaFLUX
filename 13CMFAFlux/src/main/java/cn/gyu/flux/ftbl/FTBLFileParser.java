package cn.gyu.flux.ftbl;

import cn.gyu.flux.reaction.Compound;
import cn.gyu.flux.reaction.Constant;
import cn.gyu.flux.reaction.Reaction;
import cn.gyu.flux.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTBLFileParser {

    public static String parseReactionContents(File ftblFile) {
        StringBuffer sb = new StringBuffer();
        List<String> networkBlockList = FTBLFileReader.readBlock(Constant.NETWORK_BLOCK, ftblFile);
        Map<String, String> reversedReactionMap = parseFluxXCH(ftblFile);
        List<Reaction> reactions = new ArrayList<>();
        for (int i = 1; i < networkBlockList.size(); i += 2) {
            Reaction reaction = parseReaction(networkBlockList.get(i), networkBlockList.get(i + 1));
            reactions.add(reaction);
            if (reversedReactionMap.containsKey(reaction.getName())) {
                reactions.add(reaction.generateReversedReaction());
            }
        }

        for(Reaction reaction : reactions) {
            int compoundCount = reaction.getCompoundCount();
            Compound reactant = reaction.getReactant(0);
            sb.append("<tr>");
            sb.append("<td rowspan=\"" + compoundCount + "\">");
            sb.append(reaction.getName() + "</td>");
            appendCompound(sb, reactant, true);
            sb.append("</tr>");
            for(int index = 1; index < reaction.getReactants().size(); index++) {
                reactant = reaction.getReactant(index);
                sb.append("<tr>");
                appendCompound(sb, reactant, true);
                sb.append("</tr>");
            }
            for(Compound product : reaction.getProducts()) {
                sb.append("<tr>");
                appendCompound(sb, product, false);
                sb.append("</tr>");
            }
        }
        return sb.toString();
    }

    public static String parseFluxNet(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> netBlockList = FTBLFileReader.readSubBlock(Constant.FLUXES, Constant.NET, ftblFile);
        Map<String, Integer> consFlagMap = new HashMap<>();
        consFlagMap.put("C",1);
        for (int index = 1; index < netBlockList.size(); index++) {
            String[] splits = netBlockList.get(index).split("\t");
            if(splits.length > 4) {
                if(consFlagMap.containsKey(splits[3].trim())) {
                    sb.append("<tr>");
                    sb.append("<td>" + splits[2].trim() + "</td>");
                    sb.append("<td>" + splits[4].trim() + "</td>");
                    sb.append("</tr>");
                }
            }
        }
        return sb.toString();
    }

    public static String parsePoolSize(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> poolSizeList = FTBLFileReader.readBlock(Constant.POOL_SIZE, ftblFile);
        for(int index = 1; index < poolSizeList.size(); index++) {
            String poolSize = poolSizeList.get(index);
            String[] splits = poolSize.split("\t");
            sb.append("<tr>");
            sb.append("<td>" + splits[1].trim() + "</td>");
            sb.append("<td>" + splits[2].trim() + "</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    public static String parseEqualities(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> labelEqualitiesBlockList = FTBLFileReader.readSubBlock(Constant.EQUALITIES, Constant.NET, ftblFile);
        for (int index = 1; index < labelEqualitiesBlockList.size(); index++) {
            String[] splits = labelEqualitiesBlockList.get(index).split("\t");
            String name = Util.removeCoefficient(splits[3]);
            if(name.contains("-")) {
                String[] nameSplits = name.split("-");
                sb.append("<tr>");
                sb.append("<td>" + Util.parseCoefficient(nameSplits[0].trim()) +
                        "*" + Util.removeCoefficient(nameSplits[0].trim()) + "</td>");
                sb.append("<td>" + "-" + Util.parseCoefficient(nameSplits[1].trim()) +
                        "*" + Util.removeCoefficient(nameSplits[1].trim()) + "</td>");
                sb.append("<td>" + splits[2].trim() + "</td>");
                sb.append("</tr>");
            } else if(name.contains("+")) {
                String[] nameSplits = name.split("\\+");
                sb.append("<tr>");
                sb.append("<td>" + Util.parseCoefficient(nameSplits[0].trim()) +
                        "*" + Util.removeCoefficient(nameSplits[0].trim()) + "</td>");
                sb.append("<td>" + Util.parseCoefficient(nameSplits[1].trim()) +
                        "*" + Util.removeCoefficient(nameSplits[1].trim()) + "</td>");
                sb.append("<td>" + splits[2].trim() + "</td>");
                sb.append("</tr>");
            }
        }
        return sb.toString();
    }

    public static String parseInEqualities(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> netBlockList = FTBLFileReader.readSubBlock(Constant.INEQUALITIES, Constant.NET, ftblFile);
        for (int index = 1; index < netBlockList.size(); index++) {
            String[] splits = netBlockList.get(index).split("\t");
            String rName = splits[4].trim();
            if(rName.contains("//")) {
                rName = rName.substring(0, rName.indexOf("//")).trim();
            }
            String symbol = splits[3].trim();
            if(symbol.equals("<=")) {
                symbol = ">=";
            } else if(symbol.equals(">=")) {
                symbol = "<=";
            } else if(symbol.equals(">")) {
                symbol = "<";
            } else if(symbol.equals("<")) {
                symbol = ">";
            }
            sb.append("<tr>");
            sb.append("<td>" + rName + symbol + splits[2].trim() +  "</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    public static String parseLabelInput(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> labelInputBlockList = FTBLFileReader.readBlock(Constant.LABEL_INPUT_BLOCK, ftblFile);
        String name = "";
        for (int index = 1; index < labelInputBlockList.size(); index++) {
            String labelInput = labelInputBlockList.get(index);
            String[] splits = labelInput.split("\t");
            if (!Util.isBlank(splits[1].trim())) {
                name = splits[1].trim();
            }
            String code = splits[2].trim();
            if (code.startsWith("#")) {
                code = code.substring(1);
            }
            String value = splits[3].trim();
            sb.append("<tr>");
            sb.append("<td>" + name + "</td>");
            sb.append("<td>" + code + "</td>");
            sb.append("<td>" + value + "</td>");
            sb.append("</tr>");
        }

        return sb.toString();
    }

    public static String parseMassSpectrometry(File ftblFile) {
        StringBuffer sb =  new StringBuffer();
        List<String> massBlockList = FTBLFileReader.readBlock(Constant.MASS_SPECTROMETRY, ftblFile);
        String compName = "";
        String time_point = "";
        String fragment = "";
        for(int index = 1; index < massBlockList.size(); index++) {
            String MASS_SPECTROMETRY = massBlockList.get(index);
            String[] massSplits = MASS_SPECTROMETRY.split("\t");
            if(!Util.isBlank(massSplits[1].trim())) {
                time_point = massSplits[1].trim();
            }
            if(!Util.isBlank(massSplits[2].trim())) {
                compName = massSplits[2].trim();
            }
            if(!Util.isBlank(massSplits[3].trim())) {
                fragment = massSplits[3].trim();
            }
            sb.append("<tr>");
            sb.append("<td>" + compName + "</td>");
            sb.append("<td>" + fragment + "</td>");
            sb.append("<td>" + massSplits[4].trim() + "</td>");
            sb.append("<td>" + massSplits[5].trim() + "</td>");
            sb.append("<td>" + massSplits[6].trim() + "</td>");
            sb.append("<td>" + time_point + "</td>");
            sb.append("</tr>");
        }

        return sb.toString();
    }

    private static void appendCompound(StringBuffer sb, Compound compound, boolean isReactant) {
        sb.append("<td>" + compound.getName() + "</td>");
        sb.append("<td>" + (isReactant ? "Reactant" : "Product") + "</td>");
        sb.append("<td>" + compound.getCarbon() + "</td>");
    }

    private static Map<String, String> parseFluxXCH(File ftblFile) {
        Map<String, String> reversedReactionMap = new HashMap<>();
        int reversedPosition = 3;
        List<String> xchBlockList = FTBLFileReader.readSubBlock(Constant.FLUXES, Constant.XCH, ftblFile);
        for (int index = 1; index < xchBlockList.size(); index++) {
            String[] splits = xchBlockList.get(index).split("\t");
            if (splits[reversedPosition].equals(Constant.REVERSED_REACTION_FLAG)) {
                reversedReactionMap.put(splits[reversedPosition - 1], Constant.REVERSED_REACTION_FLAG);
            } else if(splits.length >= 4) {
                if(splits[reversedPosition].equals("F") ||
                        (splits[reversedPosition].equals("C") && Double.parseDouble(splits[reversedPosition + 1]) > 0)) {
                    reversedReactionMap.put(splits[reversedPosition - 1], Constant.REVERSED_REACTION_FLAG);
                }
            }
        }
        return  reversedReactionMap;
    }

    private static Reaction parseReaction(String compoundLine, String carbonLine) {
        Reaction reaction = new Reaction();
        String[] compoundSplits = compoundLine.split("\t");
        String[] carbonSplits = carbonLine.split("\t");
        String rName = compoundSplits[1];
        rName = Util.moveBeginningNumberToEnd(rName);
        reaction.setName(rName);
        for (int index = 2; index < compoundSplits.length && index < carbonSplits.length; index++) {
            if (index >= 6) {
                break;
            }
            String name = compoundSplits[index].trim();
            String carbon = carbonSplits[index].trim();
            if (name.length() == 0 || carbon.length() == 0) {
                continue;
            }
            if (carbon.startsWith("#")) {
                carbon = carbon.substring(1);
            }
            carbon = Util.insertCommaToString(carbon);
            if (index < 4) {
                reaction.addReactant(new Compound(name, carbon));
            } else {
                if (!(name.startsWith("//") || carbon.startsWith("//"))) {
                    reaction.addProduct(new Compound(name, carbon));
                }
            }
        }
        if (reaction.getReactants().size() == 1) {
            reaction.addType(Constant.SINGLE_REACTION);
        } else {
            reaction.addType(Constant.MULTI_REACTION);
        }
        if (reaction.getProducts().size() > 2) {
            throw new RuntimeException(
                    "reaction " + reaction.getName() + " has " + reaction.getProducts().size() + " products!");
        }
        return reaction;
    }

}
