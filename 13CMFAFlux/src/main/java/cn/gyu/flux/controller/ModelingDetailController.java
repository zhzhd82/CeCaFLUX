package cn.gyu.flux.controller;

import cn.gyu.flux.db.bean.*;
import cn.gyu.flux.service.ModelingService;
import cn.gyu.flux.util.Util;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ModelingDetailController {

    @Autowired
    private ModelingService modelingService;

    private final String eType = "enzyme";
    private final String cType = "compound";
    private final String pType = "parent";
    private final String edgeType = "edgeType";

    @RequestMapping(value = "/modeling_case_detail", method = {RequestMethod.GET,RequestMethod.POST})
    public String modelDetail(@RequestParam("timestamp") Long timestamp, Model model) {
        ModelingCasesBean modelingCasesBean = modelingService.queryModelingCaseByTimestamp(timestamp);
        String reactionContents = modelingCasesBean.getComp_formula();
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        Map<String, String> reactionMap = new HashMap<>();
        List<ModelingDetailReactionBean> modelingDetailReactionBeanList = new ArrayList<>();
        String[] reactionContentSplits = reactionContents.split(outerSplitter);
        Map<String, Integer> checkMap = new HashMap<>();
        for(String reactionContent : reactionContentSplits) {
            ModelingDetailReactionBean detailReactionBean = new ModelingDetailReactionBean();
            String[] reactionComponents = reactionContent.split(innerSplitter);
            String rName = reactionComponents[0].trim();
            String formula = reactionComponents[1].trim();
            reactionMap.put(rName, formula);
            detailReactionBean.setName(rName);
            String[] formulaSplits = formula.split("\\s-->\\s");
            String reactants = formulaSplits[0].trim();
            String products = formulaSplits[1].trim();
            detailReactionBean.setReactants(reactants);
            detailReactionBean.setProducts(products);
            modelingDetailReactionBeanList.add(detailReactionBean);

            String[] reactantSplits = reactants.split("\\s\\+\\s");
            String[] productSplits = products.split("\\s\\+\\s");
            checkMap.put(reactantSplits[0].trim(), 1);
            if(reactantSplits.length == 2) {
                checkMap.put(reactantSplits[1].trim(), 1);
            }
            checkMap.put(productSplits[0], 1);
            if(productSplits.length == 2) {
                checkMap.put(productSplits[1], 1);
            }
        }

        ModelingDetailStaticsBean staticsBean = new ModelingDetailStaticsBean();
        staticsBean.setReaction_count(modelingDetailReactionBeanList.size());
        staticsBean.setCompound_count(checkMap.size());

        OptimizingResultBean optimizingResultBean = modelingService.queryOptimizingResultByTimestamp(timestamp);
        staticsBean.setMinimal_norm2(optimizingResultBean.getNorm2());
        double goodness_of_fit = optimizingResultBean.getGoodness_of_fit();
        if(goodness_of_fit < 1e-3) {
            staticsBean.setGoodness_of_fit("> 0.999");
        } else {
            staticsBean.setGoodness_of_fit((1 - goodness_of_fit) + "");
        }

        String optimizing_flux = optimizingResultBean.getOptimizing_flux();
        String flux_confidence_interval_contents = optimizingResultBean.getFlux_confidence_interval();

        Map<String, String> flux_confidence_interval_map = new HashMap<>();
        String[] confidence_interval_splits = flux_confidence_interval_contents.split(outerSplitter);
        for(String confidence_interval : confidence_interval_splits) {
            String[] confidence_interval_parts = confidence_interval.split(innerSplitter);
            flux_confidence_interval_map.put(confidence_interval_parts[0].trim(), confidence_interval_parts[1].trim());
        }

        Map<String, Double> nameFluxMap = new HashMap<>();
        String[] nameFluxSplits = optimizing_flux.split("\\s;\\s");
        for(String nameFlux : nameFluxSplits) {
            String[] splits = nameFlux.split("\\s:\\s");
            nameFluxMap.put(splits[0].trim(), Util.formatNumber(Double.parseDouble(splits[1].trim())));
        }

        for(ModelingDetailReactionBean detailReactionBean : modelingDetailReactionBeanList) {
            String reaction_name = detailReactionBean.getName();
            detailReactionBean.setValue(nameFluxMap.get(reaction_name));
            detailReactionBean.setConfidence_interval(flux_confidence_interval_map.get(reaction_name));
        }

        Map<String, String> c_confidence_interval_map = new HashMap<>();
        String[] c_confidence_interval_splits = optimizingResultBean.getC_confidence_interval().split(outerSplitter);
        for(String c_confidence_interval : c_confidence_interval_splits) {
            String[] c_confidence_interval_parts = c_confidence_interval.split(innerSplitter);
            c_confidence_interval_map.put(c_confidence_interval_parts[0].trim(), c_confidence_interval_parts[1].trim());
        }

        String c_free_value_contents = optimizingResultBean.getC_free_value();
        List<CFreeEstimatedBean> cFreeBeanList = new ArrayList<>();
        Map<String, Double> cValueMap = new HashMap<>();
        String[] c_free_value_splits = c_free_value_contents.split("\\s;\\s");
        for(String c_free_value : c_free_value_splits) {
            CFreeEstimatedBean cBean = new CFreeEstimatedBean();
            String[] name_value_splits = c_free_value.split("\\s:\\s");
            String cFreeName = name_value_splits[0].trim();
            cBean.setName(cFreeName);
            cBean.setValue(name_value_splits[1]);
            cBean.setConfidence_interval(c_confidence_interval_map.get(cFreeName));
            cFreeBeanList.add(cBean);
            cValueMap.put(cFreeName, Double.parseDouble(name_value_splits[1].trim()));
        }

        String pool_size_contents = modelingCasesBean.getPool_size();
        List<String> c_value_List = new ArrayList<>();
        c_value_List.addAll(Arrays.asList(c_free_value_contents.split(outerSplitter)));
        c_value_List.addAll(Arrays.asList(pool_size_contents.split(outerSplitter)));
        for(String c_value : c_value_List) {
            String[] c_value_splits = c_value.split(innerSplitter);
            cValueMap.put(c_value_splits[0].trim(), Double.parseDouble(c_value_splits[1].trim()));
        }

        JSONArray graphJSONArray = parseReactions(reactionMap, nameFluxMap, cValueMap);
        addNodeClass(graphJSONArray);
        String graph = graphJSONArray.toString();
        model.addAttribute("graph_data", graph);

        List<FluxNetBean> fluxNetBeanList = parseFluxNet(modelingCasesBean.getFluxes_net());
        List<PoolSizeBean> poolSizeBeanList = parsePoolSize(modelingCasesBean.getPool_size());
        List<EqualitiesBean> equalitiesBeanList = parseEqualitiesBean(modelingCasesBean.getEqualities());
        List<InEqualitiesBean> inEqualitiesBeanList = parseInEqualitiesBean(modelingCasesBean.getInequalities());
        List<LabelInputBean> labelInputBeanList = parseLabelInput(modelingCasesBean.getLabel_input());
        List<MassSpectrometryBean> massSpectrometryBeanList = parseMassSpectrometry(modelingCasesBean.getMass_spectrometry());

        model.addAttribute("statics_bean",staticsBean);
        model.addAttribute("c_free_list",cFreeBeanList);
        model.addAttribute("reaction_list",modelingDetailReactionBeanList);
        model.addAttribute("flux_net_list", fluxNetBeanList);
        model.addAttribute("pool_size_list", poolSizeBeanList);
        model.addAttribute("equalities_list", equalitiesBeanList);
        model.addAttribute("inEqualities_list", inEqualitiesBeanList);
        model.addAttribute("label_input_list", labelInputBeanList);
        model.addAttribute("mass_spectrometry_list", massSpectrometryBeanList);
        model.addAttribute("timestamp", timestamp);
        return "modeling_detail";
    }

    @ResponseBody
    @PostMapping("/download_reaction")
    public void downloadReactionFlux(@RequestParam("timestamp") long timestamp,
                                       HttpServletResponse response) {
        OptimizingResultBean optimizingResultBean = modelingService.queryOptimizingResultByTimestamp(timestamp);

        final String CSV_COLUMN_SEPARATOR = ",";
        final String CSV_RN = System.lineSeparator();
        StringBuffer reactionContentSB = new StringBuffer();
        List<NameValueConfidenceBean> nameValueConfidenceBeanList = new ArrayList<>();
        Map<String, NameValueConfidenceBean> checkMap = new HashMap<>();

        parseNameValueConfidenceContent(nameValueConfidenceBeanList,
                optimizingResultBean.getOptimizing_flux(),
                true, checkMap);

        parseNameValueConfidenceContent(nameValueConfidenceBeanList,
                optimizingResultBean.getFlux_confidence_interval(),
                false, checkMap);

        parseNameValueConfidenceContent(nameValueConfidenceBeanList,
                optimizingResultBean.getC_free_value(),
                true, checkMap);

        parseNameValueConfidenceContent(nameValueConfidenceBeanList,
                optimizingResultBean.getC_confidence_interval(),
                false, checkMap);

        reactionContentSB.append("Name" + CSV_COLUMN_SEPARATOR
                + "Value" + CSV_COLUMN_SEPARATOR
                + "Confidence interval" + CSV_RN);
        for(NameValueConfidenceBean nameValueConfidenceBean : nameValueConfidenceBeanList) {
            reactionContentSB.append(nameValueConfidenceBean.getName() + CSV_COLUMN_SEPARATOR
                    + nameValueConfidenceBean.getValue() + CSV_COLUMN_SEPARATOR
                    + "\"" + nameValueConfidenceBean.getConfidence() + "\"" + CSV_RN);
        }

        try {
            response.setContentType("application/ms-txt");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=30");
            response.setHeader("Content-Disposition", "attachment; filename=reaction_c_value_confidence_interval.csv");
            final OutputStream os = response.getOutputStream();
            os.write(reactionContentSB.toString().getBytes("UTF-8"));
            os.flush();
        } catch (Exception e) {
        }
    }

    private void parseNameValueConfidenceContent(List<NameValueConfidenceBean> nameValueConfidenceBeanList,
                                                 String nameValueConfidenceContent, boolean isValue,
                                                 Map<String, NameValueConfidenceBean> checkMap) {
        if(Util.isBlank(nameValueConfidenceContent)) {
            return;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";

        String[] nameValueConfidenceContentSplits = nameValueConfidenceContent.split(outerSplitter);
        for(String nameValueConfidenceContentSplit : nameValueConfidenceContentSplits) {
            String[] nameValueConfidenceSplits = nameValueConfidenceContentSplit.split(innerSplitter);
            NameValueConfidenceBean nameValueConfidenceBean;
            if(isValue) {
                nameValueConfidenceBean = new NameValueConfidenceBean();
                nameValueConfidenceBean.setName(nameValueConfidenceSplits[0].trim());
                nameValueConfidenceBean.setValue(nameValueConfidenceSplits[1].trim());
                checkMap.put(nameValueConfidenceBean.getName(), nameValueConfidenceBean);
                nameValueConfidenceBeanList.add(nameValueConfidenceBean);
            } else {
                nameValueConfidenceBean = checkMap.get(nameValueConfidenceSplits[0].trim());
                nameValueConfidenceBean.setConfidence(nameValueConfidenceSplits[1].trim());
            }
        }
    }

    private List<MassSpectrometryBean> parseMassSpectrometry(String massSpectrometryContents) {
        List<MassSpectrometryBean> massSpectrometryBeanList = new ArrayList<>();
        if(Util.isBlank(massSpectrometryContents)) {
            return massSpectrometryBeanList;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        String[] massSpectrometryContentSplits = massSpectrometryContents.split(outerSplitter);
        for(String massSpectrometryContentSplit : massSpectrometryContentSplits) {
            String[] massSpectrometrySplits = massSpectrometryContentSplit.split(innerSplitter);
            massSpectrometryBeanList.add(new MassSpectrometryBean(
                    massSpectrometrySplits[0].trim(),
                    massSpectrometrySplits[1].trim(),
                    massSpectrometrySplits[2].trim(),
                    massSpectrometrySplits[3].trim(),
                    massSpectrometrySplits[4].trim(),
                    massSpectrometrySplits[5].trim()
            ));
        }
        return massSpectrometryBeanList;
    }

    private List<LabelInputBean> parseLabelInput(String labelInputContents) {
        List<LabelInputBean> labelInputBeanList = new ArrayList<>();
        if(Util.isBlank(labelInputContents)) {
            return labelInputBeanList;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        String[] labelInputContentSplits = labelInputContents.split(outerSplitter);
        for(String labelInputContentSplit : labelInputContentSplits) {
            String[] labelInputSplits = labelInputContentSplit.split(innerSplitter);
            labelInputBeanList.add(new LabelInputBean(labelInputSplits[0].trim(), labelInputSplits[1].trim(), labelInputSplits[2].trim()));
        }
        return labelInputBeanList;
    }

    private List<InEqualitiesBean> parseInEqualitiesBean(String inEqualitiesContents) {
        List<InEqualitiesBean> inEqualitiesBeanList = new ArrayList<>();
        if(Util.isBlank(inEqualitiesContents)) {
            return inEqualitiesBeanList;
        }
        String outerSplitter = " ; ";
        String[] inEqualitiesContentSplits = inEqualitiesContents.split(outerSplitter);
        for(String inEqualitiesContent : inEqualitiesContentSplits) {
            inEqualitiesBeanList.add(new InEqualitiesBean(inEqualitiesContent.trim()));
        }
        return inEqualitiesBeanList;
    }

    private List<EqualitiesBean> parseEqualitiesBean(String equalitiesContents) {
        List<EqualitiesBean> equalitiesBeanList = new ArrayList<>();
        if(Util.isBlank(equalitiesContents)) {
            return equalitiesBeanList;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        String[] equalitiesContentSplits = equalitiesContents.split(outerSplitter);
        for(String equalitiesContentSplit : equalitiesContentSplits) {
            String[] equalitiesSplits = equalitiesContentSplit.split(innerSplitter);
            equalitiesBeanList.add(new EqualitiesBean(equalitiesSplits[0].trim(), equalitiesSplits[1].trim(), equalitiesSplits[2].trim()));
        }
        return equalitiesBeanList;
    }

    private List<PoolSizeBean> parsePoolSize(String poolSizeContents) {
        List<PoolSizeBean> poolSizeBeanList = new ArrayList<>();
        if(Util.isBlank(poolSizeContents)) {
            return poolSizeBeanList;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        String[] poolSizeContentSplits = poolSizeContents.split(outerSplitter);
        for(String poolSizeContentSplit : poolSizeContentSplits) {
            String[] poolSizeSplits = poolSizeContentSplit.split(innerSplitter);
            poolSizeBeanList.add(new PoolSizeBean(poolSizeSplits[0].trim(), poolSizeSplits[1].trim()));
        }
        return poolSizeBeanList;
    }

    private List<FluxNetBean> parseFluxNet(String fluxNetContents) {
        List<FluxNetBean> fluxNetBeanList = new ArrayList<>();
        if(Util.isBlank(fluxNetContents)) {
            return fluxNetBeanList;
        }
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        String[] fluxNetContentSplits = fluxNetContents.split(outerSplitter);
        for(String fluxNetContentSplit : fluxNetContentSplits) {
            String[] fluxNetSplits = fluxNetContentSplit.split(innerSplitter);
            fluxNetBeanList.add(new FluxNetBean(fluxNetSplits[0].trim(), fluxNetSplits[1].trim()));
        }
        return fluxNetBeanList;
    }

    private void addNodeClass(JSONArray graphJSONArray){
        for(int i=0;i<graphJSONArray.size();i++) {
            JSONObject obj = graphJSONArray.getJSONObject(i).getJSONObject("data");
            String type = obj.getString("type");
            if(type == null || type.length() == 0 || type.equals(edgeType)) {
                continue;
            }
            String cls;
            if(type.equals(eType)) {
                cls = "enzyme";
            } else {
                cls = "compound";
            }
            graphJSONArray.getJSONObject(i).put("classes", cls);
        }
    }

    private JSONArray parseReactions(Map<String,String> reactionMap, Map<String, Double> nameFluxMap, Map<String, Double> cValueMap) {
        Map<String,String> nodeMap = new HashMap<>();
        String parentId = "parent_box_node";
        JSONArray graphJSONArray = new JSONArray();
        graphJSONArray.add(generateParentNodeJSON(parentId, pType,""));
        int count = 0;
        for(String enzyme : reactionMap.keySet()) {
            String r_value = Util.roundNumber(nameFluxMap.get(enzyme), 1) + "";
            String eId = "eid_" + count;
            count++;
            graphJSONArray.add(generateNodeJSON(eId,eType,parentId,enzyme, null));
            String formula = reactionMap.get(enzyme);
            String[] splits = formula.split("\\s-->\\s");
            String[] substrates = splits[0].split("\\s\\+\\s");
            String[] products = splits[1].split("\\s\\+\\s");
            for (String substrate : substrates) {
                substrate = removeCompoundCoefficient(substrate);
                String sId;
                if (!nodeMap.containsKey(substrate)) {
                    sId = "sid_" + count;
                    count++;
                    nodeMap.put(substrate, sId);
                    graphJSONArray.add(generateNodeJSON(sId,cType,parentId,substrate, cValueMap.get(substrate) + ""));
                } else {
                    sId = nodeMap.get(substrate);
                }
                graphJSONArray.add(generateEdgeJSON(sId + "_" + eId,sId,eId,"bezier", r_value));
            }
            for (String product : products) {
                product = removeCompoundCoefficient(product);
                String pId;
                if (!nodeMap.containsKey(product)) {
                    pId = "sid_" + count;
                    count++;
                    nodeMap.put(product, pId);
                    graphJSONArray.add(generateNodeJSON(pId,cType,parentId,product, cValueMap.get(product) + ""));
                } else {
                    pId = nodeMap.get(product);
                }
                graphJSONArray.add(generateEdgeJSON(pId + "_" + eId,eId,pId,"bezier", r_value));
            }
        }
        return graphJSONArray;
    }

    private JSONObject generateNodeJSON(String id, String type, String pId, String label, String value) {
        JSONObject result = new JSONObject();
        JSONObject tmp = new JSONObject();
        tmp.put("id", id);
        tmp.put("type", type);
        tmp.put("parent", pId);
        tmp.put("label", label);
        if(value != null) {
            if(value.equals("null")) {
                value = "0";
            }
            double width = Double.parseDouble(value);
            width = width * 20 + 10;
            tmp.put("width", width);
        }
        result.put("data", tmp);
        return result;
    }

    private JSONObject generateParentNodeJSON(String id, String type,String label) {
        JSONObject result = new JSONObject();
        JSONObject tmp = new JSONObject();
        tmp.put("id", id);
        tmp.put("type", type);
        tmp.put("label", label);
        result.put("data", tmp);
        return result;
    }

    private JSONObject generateEdgeJSON(String id, String src, String target,String cls, String label) {
        double width = Double.parseDouble(label);
        if(width < 0.1) {
            width = 0.5;
        }
        width = width * 30 / 6;
        JSONObject result = new JSONObject();
        JSONObject tmp = new JSONObject();
        tmp.put("id", id);
        tmp.put("source", src);
        tmp.put("type", edgeType);
        tmp.put("target", target);
        tmp.put("width", width+"");
        tmp.put("label", label);
        result.put("data", tmp);
        result.put("classes", cls);
        return result;
    }

    public String removeCompoundCoefficient(String compound) {
        String repr = "^[0-9]+\\s+";
        Pattern pattern = Pattern.compile(repr);
        Matcher matcher = pattern.matcher(compound);
        if (matcher.find()) {
            compound = compound.substring(matcher.end());
        }
        return compound;
    }

}
