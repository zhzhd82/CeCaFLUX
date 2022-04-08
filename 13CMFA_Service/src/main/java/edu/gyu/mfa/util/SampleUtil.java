package edu.gyu.mfa.util;

import edu.gyu.mfa.ftbl.AbsConsNameValue;
import edu.gyu.mfa.ftbl.Equality;
import edu.gyu.mfa.ftbl.FTBLFile;
import edu.gyu.mfa.ftbl.InEquality;
import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.reaction.Reaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SampleUtil {

    private static List<List<String>> sampleReactionFluxByPython(List<Reaction> reactions, List<String> exReactionList,
                                                     List<List<String>> constraisContentList, List<String> equalityList, String python) {
        String reaction_splitter = "#";
        String reaction_name_splitter = ":";
        String equation_splitter = "=";
        String metabolite_splitter = "+";
        String cons_splitter = "#";
        String cons_element_splitter = "@";
        String cons_element_coefficient_splitter = ":";
        int number = Argument.sample_count;
        double lower_bound = Argument.lower_bound;
        double upper_bound = Argument.upper_bound;
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
        String sampling_path = Util.parsePath(Argument.sampling_proc_file) + File.separator;
        String reactions_content_file = sampling_path + "reaction_content_string";
        Util.writeContentToFile(reactions_content_file, reactionSB.toString());

        String constrains_content_splitter = "#constrains_splitter#";
        StringBuffer consNameValueSB = new StringBuffer();
        for (int consIndex = 0; consIndex < constraisContentList.size(); consIndex++) {
            List<String> consNameValueList = constraisContentList.get(consIndex);
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
            if(consIndex < constraisContentList.size() - 1) {
                consNameValueSB.append(constrains_content_splitter);
            }
        }

        String constrains_content_file = sampling_path + "constrains_content_string";
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

        String result_file = sampling_path + "sampling_result";
        try {
            Process proc = Runtime.getRuntime().exec(python + " " + Argument.sampling_proc_file + " "
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

    private static List<List<String>> sampleReactionFluxByPythonOptimizer(List<Reaction> reactions, List<String> exReactionList,
                                                     List<List<String>> constraisContentList, List<String> equalityList, String python) {
        String reaction_splitter = "#";
        String reaction_name_splitter = ":";
        String equation_splitter = "=";
        String metabolite_splitter = "+";
        String cons_splitter = "#";
        String cons_element_splitter = "@";
        String cons_element_coefficient_splitter = ":";
        double lower_bound = Argument.lower_bound + 0.1;
        double upper_bound = Argument.upper_bound - 0.1;
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
        String sampling_path = Util.parsePath(Argument.sampling_proc_file) + File.separator;
        String reactions_content_file = sampling_path + "reaction_content_string_optimizer";
        Util.writeContentToFile(reactions_content_file, reactionSB.toString());

        String constrains_content_splitter = "#constrains_splitter#";
        StringBuffer consNameValueSB = new StringBuffer();
        for (int consIndex = 0; consIndex < constraisContentList.size(); consIndex++) {
            List<String> consNameValueList = constraisContentList.get(consIndex);
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
            if(consIndex < constraisContentList.size() - 1) {
                consNameValueSB.append(constrains_content_splitter);
            }
        }

        String constrains_content_file = sampling_path + "constrains_content_string_optimizer";
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

        String result_file = sampling_path + "optimizer_result";
        String optimizer_file = sampling_path + "FluxSamplingWithOptimizer.py";
        try {
            Process proc = Runtime.getRuntime().exec(python + " " + optimizer_file + " "
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
                    + "--upper_bound " + upper_bound);
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> optimizerResultList = Util.readFileContent(result_file);
        String optimizer_start_tag = "########### optimizer value start ###########";
        List<List<String>> results = new ArrayList<>();
        List<String> optimizer_result_list = null;
        for(String optimizer_result : optimizerResultList) {
            if(optimizer_result.equals(optimizer_start_tag)) {
                optimizer_result_list = new ArrayList<>();
                results.add(optimizer_result_list);
            } else {
                optimizer_result_list.add(optimizer_result);
            }
        }
        return results;
    }

    public static List<List<String>> sampleReactionFluxWithFile(List<Reaction> reactions, FTBLFile ftblFile, String python, List<List<AbsConsNameValue>> constraisList) {

        List<String> equalities = new ArrayList<>();

        for (Equality equality : ftblFile.getEqualityList()) {
            String equal = "";
            for(String name : equality.getKeySet()) {
                double coefficient = equality.getCoefficient(name);
                equal = equal + coefficient + ":" + name + ",";
            }
            equal = equal + equality.getValue();
            equalities.add(equal);
        }

        List<String> consNameValueList = new ArrayList<>();
        for(InEquality inEquality : ftblFile.getInequalityList()) {
            consNameValueList.add(inEquality.getInEqualityStr());
        }
        List<AbsConsNameValue> tmpList = new ArrayList<>();
        tmpList.addAll(ftblFile.getFluxNetList());

        for(AbsConsNameValue absConsNameValue : tmpList) {
            consNameValueList.add(absConsNameValue.getReaction_name()
                    + "," + absConsNameValue.getValue()
                    + "," + absConsNameValue.getValue());
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
        if(constraisList == null) {
            List<String> contentList = new ArrayList<>();
            contentList.addAll(consNameValueList);
            constrainsContentList.add(contentList);
        } else {
            for(List<AbsConsNameValue> absConsNameValueList : constraisList) {
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

        return sampleReactionFluxByPython(reactions, exReactionList, constrainsContentList, equalities, python);
    }

    public static List<List<String>> sampleReactionFluxWithOptimizer(List<Reaction> reactions, FTBLFile ftblFile, String python, List<List<AbsConsNameValue>> constraisList) {

        List<String> equalities = new ArrayList<>();

        for (Equality equality : ftblFile.getEqualityList()) {
            String equal = "";
            for(String name : equality.getKeySet()) {
                double coefficient = equality.getCoefficient(name);
                equal = equal + coefficient + ":" + name + ",";
            }
            equal = equal + equality.getValue();
            equalities.add(equal);
        }

        List<String> consNameValueList = new ArrayList<>();
        for(InEquality inEquality : ftblFile.getInequalityList()) {
            consNameValueList.add(inEquality.getInEqualityStr());
        }
        List<AbsConsNameValue> tmpList = new ArrayList<>();
        tmpList.addAll(ftblFile.getFluxNetList());

        for(AbsConsNameValue absConsNameValue : tmpList) {
            consNameValueList.add(absConsNameValue.getReaction_name()
                    + "," + absConsNameValue.getValue()
                    + "," + absConsNameValue.getValue());
        }

        List<String> exReactionList = new ArrayList<>();
        for(Reaction reaction : reactions) {
            consNameValueList.add(reaction.getName() + "," + 0.1 + "," + "None");
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
        if(constraisList == null) {
            List<String> contentList = new ArrayList<>();
            contentList.addAll(consNameValueList);
            constrainsContentList.add(contentList);
        } else {
            for(List<AbsConsNameValue> absConsNameValueList : constraisList) {
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

        return sampleReactionFluxByPythonOptimizer(reactions, exReactionList, constrainsContentList, equalities, python);
    }

}
