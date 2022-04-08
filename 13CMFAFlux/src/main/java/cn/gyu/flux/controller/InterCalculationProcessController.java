package cn.gyu.flux.controller;

import cn.gyu.flux.db.bean.InterProcessResultBean;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.service.ModelingService;
import cn.gyu.flux.util.Util;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class InterCalculationProcessController {

    @Autowired
    private ModelingService modelingService;

    private final String eType = "enzyme";
    private final String cType = "compound";
    private final String pType = "parent";
    private final String edgeType = "edgeType";

    @ResponseBody
    @PostMapping("/get_flux_graph_data")
    public Map getFluxGraphData(@RequestParam(value = "timestamp") long timestamp,
                                    @RequestParam(value = "count") int count) {
        Map<String, String> resultMap = new HashMap<>();
        ModelingCasesBean modelingCasesBean = modelingService.queryModelingCaseByTimestamp(timestamp);
        if(modelingCasesBean.getStatus() == 3 || modelingCasesBean.getStatus() == 4) {
            resultMap.put("finished", "yes");
        } else {
            resultMap.put("finished", "no");
        }
        List<InterProcessResultBean> interProcessResultBeanList = modelingService.findInterProcessResultBeanByModelIdAndCount(modelingCasesBean.getId(), count);
        if(interProcessResultBeanList.size() > 0) {
            InterProcessResultBean interProcessResultBean = interProcessResultBeanList.get(interProcessResultBeanList.size() - 1);
            String reactionContents = modelingCasesBean.getComp_formula();
            String innerSplitter = " : ";
            String outerSplitter = " ; ";
            Map<String, String> reactionMap = new HashMap<>();
            String[] reactionContentSplits = reactionContents.split(outerSplitter);
            Map<String, Integer> checkMap = new HashMap<>();
            for(String reactionContent : reactionContentSplits) {
                String[] reactionComponents = reactionContent.split(innerSplitter);
                String rName = reactionComponents[0].trim();
                String formula = reactionComponents[1].trim();
                reactionMap.put(rName, formula);
                String[] formulaSplits = formula.split("\\s-->\\s");
                String reactants = formulaSplits[0].trim();
                String products = formulaSplits[1].trim();

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

            Map<String, Double> nameFluxMap = new HashMap<>();
            String[] nameFluxSplits = interProcessResultBean.getFlux_value().split(outerSplitter);
            for(String nameFlux : nameFluxSplits) {
                String[] splits = nameFlux.split(innerSplitter);
                nameFluxMap.put(splits[0].trim(), Util.formatNumber(Double.parseDouble(splits[1].trim())));
            }

            Map<String, Double> cValueMap = new HashMap<>();
            String c_free_value_contents = interProcessResultBean.getC_free_value();
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
            resultMap.put("graph_data", graph);
            resultMap.put("status", "success");
            resultMap.put("count", interProcessResultBean.getCount()+"");
            resultMap.put("calculating_status", modelingCasesBean.getStatus() + "");
        } else {
            resultMap.put("status", "waiting");
        }
        return resultMap;
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
            width = width * 10 + 5;
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

    @ResponseBody
    @GetMapping("/get_line_graph_data")
    public Map getLineGraphData(@RequestParam(value = "timestamp") long timestamp,
                                @RequestParam(value = "count") int count) {
        Map<String, String> resultMap = new HashMap<>();
        ModelingCasesBean modelingCasesBean = modelingService.queryModelingCaseByTimestamp(timestamp);
        List<InterProcessResultBean> interProcessResultBeanList = modelingService.findInterProcessResultBeanByModelIdAndSort(modelingCasesBean.getId());
        if(interProcessResultBeanList.size() > 0) {
            int lastCount = interProcessResultBeanList.get(interProcessResultBeanList.size() - 1).getCount();
            if(lastCount > count) {
                resultMap.put("status", "success");
                resultMap.put("count",  lastCount + "");
                JSONArray xArray = new JSONArray();
                JSONArray parentMeanNorm2Array = new JSONArray();
                JSONArray crossMeanNorm2Array = new JSONArray();
                JSONArray mutationMeanNorm2Array = new JSONArray();
                JSONArray parentMinNorm2Array = new JSONArray();
                JSONArray crossMinNorm2Array = new JSONArray();
                JSONArray mutationMinNorm2Array = new JSONArray();
                for(InterProcessResultBean interProcessResultBean : interProcessResultBeanList) {
                    xArray.add(interProcessResultBean.getCount() + "");
                    parentMeanNorm2Array.add(Math.log10(interProcessResultBean.getParent_mean_norm2()));
                    parentMinNorm2Array.add(Math.log10(interProcessResultBean.getParent_min_norm2()));
                    crossMeanNorm2Array.add(Math.log10(interProcessResultBean.getCross_mean_norm2()));
                    crossMinNorm2Array.add(Math.log10(interProcessResultBean.getCross_min_norm2()));
                    mutationMeanNorm2Array.add(Math.log10(interProcessResultBean.getMutation_mean_norm2()));
                    mutationMinNorm2Array.add(Math.log10(interProcessResultBean.getMutation_min_norm2()));
                }
                resultMap.put("xAxis", xArray.toJSONString());
                resultMap.put("parent_mean_norm2", parentMeanNorm2Array.toJSONString());
                resultMap.put("parent_min_norm2", parentMinNorm2Array.toJSONString());
                resultMap.put("cross_mean_norm2", crossMeanNorm2Array.toJSONString());
                resultMap.put("cross_min_norm2", crossMinNorm2Array.toJSONString());
                resultMap.put("mutation_mean_norm2", mutationMeanNorm2Array.toJSONString());
                resultMap.put("mutation_min_norm2", mutationMinNorm2Array.toJSONString());
            } else {
                resultMap.put("status", "no_new_data");
            }
        } else {
            resultMap.put("status", "waiting");
        }
        return resultMap;
    }

}
