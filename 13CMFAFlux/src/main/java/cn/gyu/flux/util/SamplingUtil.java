package cn.gyu.flux.util;

import cn.gyu.flux.config.HTTPConstant;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.reaction.AbsConsNameValue;
import cn.gyu.flux.reaction.Constant;
import cn.gyu.flux.reaction.Reaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SamplingUtil {

    public static List<List<String>> sampleReactionFluxWithFile(List<Reaction> reactions, ModelingCasesBean modelingCasesBean, List<List<AbsConsNameValue>> constrainsList) {
        String innerSplitter = " : ";
        String outerSplitter = " ; ";

        List<String> equalitiesList = new ArrayList<>();
        String[] equalitiesSplits = modelingCasesBean.getEqualities().split(outerSplitter);
        for(String equalities : equalitiesSplits) {
            if(StringUtil.isBlank(equalities)) {
                continue;
            }
            String[] partSplits = equalities.split(innerSplitter);
            String part_0 = partSplits[0];
            String part_1 = partSplits[1];
            String part_2 = partSplits[2];
            String[] part_0_splits = part_0.split("\\*");
            String equal = part_0_splits[0].trim() + ":" + part_0_splits[1].trim() + ",";
            String[] part_1_splits = part_1.split("\\*");
            equal = equal + part_1_splits[0].trim() + ":" + part_1_splits[1].trim() + ",";
            equal = equal + part_2.trim();
            equalitiesList.add(equal);
        }

        List<String> consNameValueList = new ArrayList<>();
        String[] inEqualitiesSplits = modelingCasesBean.getInequalities().split(outerSplitter);
        for(String inEqualities : inEqualitiesSplits) {
            if(StringUtil.isBlank(inEqualities)) {
                continue;
            }
            String lb_str, ub_str;
            String none = "None";
            String[] partSplits;
            if(inEqualities.contains("<=")) {
                partSplits = inEqualities.split("<=");
                ub_str = partSplits[1].trim();
                consNameValueList.add(partSplits[0].trim() + "," + none + "," + ub_str);
            } else if(inEqualities.contains(">=")) {
                partSplits = inEqualities.split(">=");
                lb_str = partSplits[1].trim();
                consNameValueList.add(partSplits[0].trim() + "," + lb_str + "," + none);
            }
        }

        String[] fluxNetSplits = modelingCasesBean.getFluxes_net().split(outerSplitter);
        for(String fluxNet : fluxNetSplits) {
            if(StringUtil.isBlank(fluxNet)) {
                continue;
            }
            String[] partSplits = fluxNet.split(innerSplitter);
            consNameValueList.add(partSplits[0].trim() + "," + partSplits[1].trim() + "," + partSplits[1].trim());
        }

        List<String> exReactionList = new ArrayList<>();
        for(Reaction reaction : reactions) {
            consNameValueList.add(reaction.getName() + "," + 0 + "," + "None");
            String rName = reaction.getName() + "_ex";
            if(reaction.getType().contains(Constant.INPUT_REACTION)) {
                exReactionList.add(rName + "," + reaction.getReactant(0).getName());
                consNameValueList.add(rName + "," + "None" + "," + 0);
            } else if(reaction.getType().contains(Constant.OUTPUT_REACTION)) {
                exReactionList.add(rName + "," + reaction.getProduct(0).getName());
                consNameValueList.add(rName + "," + 0 + "," + "None");
            }
        }

        List<List<String>> constrainsContentList = new ArrayList<>();
        if(constrainsList == null) {
            List<String> contentList = new ArrayList<>();
            contentList.addAll(consNameValueList);
            constrainsContentList.add(contentList);
        } else {
            for(List<AbsConsNameValue> absConsNameValueList : constrainsList) {
                List<String> contentList = new ArrayList<>();
                contentList.addAll(consNameValueList);
                for(AbsConsNameValue absConsNameValue : absConsNameValueList) {
                    contentList.add(absConsNameValue.getReaction_name()
                            + "," + absConsNameValue.getValue()
                            + "," + absConsNameValue.getValue());
                }
                constrainsContentList.add(contentList);
            }
        }

        return sampleReactionFluxByPython(reactions, exReactionList, constrainsContentList, equalitiesList, HTTPConstant.PYTHON);
    }

    private static List<List<String>> sampleReactionFluxByPython(List<Reaction> reactions, List<String> exReactionList,
                                                                 List<List<String>> constrainsContentList, List<String> equalityList, String python) {
        String reaction_splitter = "#";
        String reaction_name_splitter = ":";
        String equation_splitter = "=";
        String metabolite_splitter = "+";
        String cons_splitter = "#";
        String cons_element_splitter = "@";
        String cons_element_coefficient_splitter = ":";
        int number = 1;
        double lower_bound = -6;
        double upper_bound = 6;
        StringBuffer reactionSB = new StringBuffer();
        for(int rIndex = 0; rIndex < reactions.size(); rIndex++) {
            Reaction reaction = reactions.get(rIndex);
            reactionSB.append(reaction.getName() + reaction_name_splitter);
            for(int cIndex = 0; cIndex < reaction.getReactants().size(); cIndex++) {
                reactionSB.append(reaction.getReactant(cIndex).getName());
                if(cIndex < reaction.getReactants().size() - 1) {
                    reactionSB.append(metabolite_splitter);
                }
            }
            reactionSB.append(equation_splitter);
            for(int pIndex = 0; pIndex < reaction.getProducts().size(); pIndex++) {
                reactionSB.append(reaction.getProduct(pIndex).getName());
                if(pIndex < reaction.getProducts().size() - 1) {
                    reactionSB.append(metabolite_splitter);
                }
            }
            if(rIndex < reactions.size() - 1) {
                reactionSB.append(reaction_splitter);
            }
        }
        for(String exReaction : exReactionList) {
            reactionSB.append(reaction_splitter);
            String[] splits = exReaction.split(",");
            reactionSB.append(splits[0] + reaction_name_splitter + splits[1] + equation_splitter + "_ex");
        }
        String sampling_path = Util.parsePath(HTTPConstant.sampling_proc_file) + File.separator;
        String reactions_content_file = sampling_path + "reaction_content_string_" + System.currentTimeMillis();
        Util.writeContentToFile(reactions_content_file, reactionSB.toString());

        String constrains_content_splitter = "#constrains_splitter#";
        StringBuffer consNameValueSB = new StringBuffer();
        for (int consIndex = 0; consIndex < constrainsContentList.size(); consIndex++) {
            List<String> consNameValueList = constrainsContentList.get(consIndex);
            for(int index = 0; index < consNameValueList.size(); index++) {
                String consNameValue = consNameValueList.get(index);
                String[] splits = consNameValue.split(",");
                consNameValueSB.append(splits[0]);
                consNameValueSB.append(cons_element_splitter);
                consNameValueSB.append(splits[1]);
                consNameValueSB.append(cons_element_splitter);
                consNameValueSB.append(splits[2]);
                if(index < consNameValueList.size() - 1) {
                    consNameValueSB.append(cons_splitter);
                }
            }
            if(consIndex < constrainsContentList.size() - 1) {
                consNameValueSB.append(constrains_content_splitter);
            }
        }

        String constrains_content_file = sampling_path + "constrains_content_string_" + System.currentTimeMillis();
        Util.writeContentToFile(constrains_content_file, consNameValueSB.toString());

        StringBuffer equalitySB = new StringBuffer();
        if(equalityList.size() == 0) {
            equalitySB.append("None");
        } else {
            for(int index = 0; index < equalityList.size(); index++) {
                String[] splits = equalityList.get(index).split(",");
                equalitySB.append(splits[0]);
                equalitySB.append(cons_element_splitter);
                equalitySB.append(splits[1]);
                equalitySB.append(cons_element_splitter);
                equalitySB.append(splits[2]);
                if(index < equalityList.size() - 1) {
                    equalitySB.append(cons_splitter);
                }
            }
        }

        String result_file = sampling_path + "sampling_result_" + System.currentTimeMillis();
        try {
            Process proc = Runtime.getRuntime().exec(python + " " + HTTPConstant.sampling_proc_file + " "
                    + "--reactions_content_file " + reactions_content_file + " "
                    + "--constrains_content_splitter " + constrains_content_splitter + " "
                    + "--constrains_content_file " + constrains_content_file + " "
                    + "--result_file " + result_file + " "
                    + "--equalities_string " + equalitySB.toString() + " "
                    + "--reaction_splitter " + reaction_splitter + " "
                    + "--reaction_name_splitter " + reaction_name_splitter + " "
                    + "--equation_splitter " + equation_splitter + " "
                    + "--cons_splitter " + cons_splitter + " "
                    + "--cons_element_splitter " + cons_element_splitter + " "
                    + "--cons_element_coefficient_splitter " + cons_element_coefficient_splitter + " "
                    + "--metabolite_splitter " + metabolite_splitter + " "
                    + "--lower_bound " + lower_bound + " "
                    + "--upper_bound " + upper_bound + " "
                    + "--number " + number);
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> samplingResultList = Util.readFileContent(result_file);
        String constrains_sampling_start_tag = "########### Constrains sample value start ###########";
        List<List<String>> results = new ArrayList<>();
        List<String> sampling_result_list = null;
        for(String sampling_result : samplingResultList) {
            if(sampling_result.equals(constrains_sampling_start_tag)) {
                sampling_result_list = new ArrayList<>();
                results.add(sampling_result_list);
            } else {
                sampling_result_list.add(sampling_result);
            }
        }
        return results;
    }

}
