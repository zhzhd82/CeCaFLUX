package cn.gyu.flux.controller;

import cn.gyu.flux.config.HTTPConstant;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.reaction.FluxEquationMatrix;
import cn.gyu.flux.reaction.Reaction;
import cn.gyu.flux.reaction.ReactionTool;
import cn.gyu.flux.service.MailService;
import cn.gyu.flux.service.ModelingService;
import cn.gyu.flux.util.SamplingUtil;
import cn.gyu.flux.util.StringUtil;
import cn.gyu.flux.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReactionController {

    @Autowired
    private ModelingService modelingService;

    @Autowired
    private MailService mailService;

    @ResponseBody
    @PostMapping("/reaction_parsing")
    public Map<String, String> parseNetworkInfo(
            @RequestParam(value = "reaction_content") String reactionContents,
            @RequestParam(value = "flux_net_content") String fluxNetContents,
            @RequestParam(value = "pool_size_content") String poolSizeContents,
            @RequestParam(value = "equality_content") String equalitiesContents,
            @RequestParam(value = "inequality_content") String inEqualitiesContents,
            @RequestParam(value = "label_input_content") String labelInputContents,
            @RequestParam(value = "mass_spectrometry_content") String massSpectrometryContents,
            @RequestParam(value = "model_name") String modelName,
            @RequestParam(value = "method") int method,
            @RequestParam(value = "step") String step,
            @RequestParam(value = "sample_space") String sample_space,
            @RequestParam(value = "email") String email,
            @RequestParam(value = "timestamp") long timestamp,
            @RequestParam(value = "src") String src,
            @RequestParam(value = "is_public", required = false, defaultValue = "2") String is_public) {
        Map<String, String> map = new HashMap<>();
        try{
            ModelingCasesBean modelingCasesBean = new ModelingCasesBean();
            if(method == 1) {
                modelingCasesBean.setMethod("adaptive");
            } else {
                modelingCasesBean.setMethod("fixed");
            }
            modelingCasesBean.setEmail(email);
            modelingCasesBean.setSample_space(Integer.parseInt(sample_space));
            modelingCasesBean.setStep(step);
            modelingCasesBean.setName(modelName);
            parseReactionContents(reactionContents, modelingCasesBean);
            parseFluxNet(fluxNetContents, modelingCasesBean);
            parsePoolSize(poolSizeContents, modelingCasesBean);
            parseEqualities(equalitiesContents, modelingCasesBean);
            parseInEqualities(inEqualitiesContents, modelingCasesBean);
            parseLabelInput(labelInputContents, modelingCasesBean);
            parseMassSpectrometry(massSpectrometryContents, modelingCasesBean);
            modelingCasesBean.setTimestamp(timestamp);
            modelingCasesBean.setStatus(1);
            modelingCasesBean.setIs_public(is_public);

            List<List<String>> reactionFluxesList = sampleReactionFlux(modelingCasesBean);
            if(reactionFluxesList.size() == 0) {
                map.put("status", "error");
            } else {
                modelingService.addModelingCaseBean(modelingCasesBean);
                map.put("status", "success");
                map.put("timestamp", "" + modelingCasesBean.getTimestamp());
                sendEmail(modelingCasesBean, src);
            }
        }catch (Exception e) {
            e.printStackTrace();
            map.put("status", "error");
        }
        return map;
    }

    private void sendEmail(ModelingCasesBean modelingCasesBean, String src) {
        if(Util.isBlank(modelingCasesBean.getEmail())) {
            return;
        }
        String subject = "CeCaFlux calculating result";
        String result_url = HTTPConstant.RESULT_URL_PREFIX + "src=" + src + "&amp;timestamp=" + modelingCasesBean.getTimestamp();
        StringBuffer contentSB = new StringBuffer();
        contentSB.append("Thanks for submitting data! You can check the calculating result at ");
        contentSB.append("<a href=\"" + result_url + "\">" + result_url + "</a>");
        mailService.sendEmail(modelingCasesBean.getEmail(), subject, contentSB.toString());
    }

    private List<List<String>> sampleReactionFlux(ModelingCasesBean modelingCasesBean) {
        List<Reaction> reactions = ReactionTool.parseNetwork(modelingCasesBean.getComp_formula());
        FluxEquationMatrix fluxEquationMatrix = new FluxEquationMatrix();
        fluxEquationMatrix.computeStoichiometricMatrix(reactions, false);
        for (Reaction reaction : reactions) {
            reaction.parseType(fluxEquationMatrix);
        }
        List<List<String>> reactionFluxSampledList = new ArrayList<>();
        try {
            reactionFluxSampledList = SamplingUtil.sampleReactionFluxWithFile(reactions, modelingCasesBean, null);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return reactionFluxSampledList;
    }

    private void parseMassSpectrometry(String massSpectrometryContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(massSpectrometryContents, "<tr>", "</tr>");
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer massSpectrometrySB = new StringBuffer();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 6) {
                massSpectrometrySB.append(tdContentList.get(0).trim() + innerSplitter +
                                tdContentList.get(1).trim() + innerSplitter +
                                tdContentList.get(2).trim() + innerSplitter +
                                tdContentList.get(3).trim() + innerSplitter +
                                tdContentList.get(4).trim() + innerSplitter +
                                tdContentList.get(5).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(massSpectrometrySB, outerSplitter);
        modelingCasesBean.setMass_spectrometry(massSpectrometrySB.toString());
    }

    private void parseLabelInput(String labelInputContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(labelInputContents, "<tr>", "</tr>");
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer labelInputSB = new StringBuffer();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 3) {
                labelInputSB.append(
                        tdContentList.get(0).trim() + innerSplitter +
                                tdContentList.get(1).trim() + innerSplitter +
                                tdContentList.get(2).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(labelInputSB, outerSplitter);
        modelingCasesBean.setLabel_input(labelInputSB.toString());
    }

    private void parseInEqualities(String inEqualitiesContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(inEqualitiesContents, "<tr>", "</tr>");
        String outerSplitter = " ; ";
        StringBuffer inEqualitiesSB = new StringBuffer();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 1) {
                inEqualitiesSB.append(tdContentList.get(0).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(inEqualitiesSB, outerSplitter);
        modelingCasesBean.setInequalities(inEqualitiesSB.toString());
    }

    private void parseEqualities(String equalitiesContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(equalitiesContents, "<tr>", "</tr>");
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer equalitiesSB = new StringBuffer();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 3) {
                equalitiesSB.append(
                        tdContentList.get(0).trim() + innerSplitter +
                        tdContentList.get(1).trim() + innerSplitter +
                        tdContentList.get(2).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(equalitiesSB, outerSplitter);
        modelingCasesBean.setEqualities(equalitiesSB.toString());
    }

    private void parsePoolSize(String poolSizeContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(poolSizeContents, "<tr>", "</tr>");
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer poolSizeSB = new StringBuffer();
        Map<String, Integer> checkMap = new HashMap<>();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 2) {
                String compName = tdContentList.get(0).trim();
                if(checkMap.containsKey(compName)) {
                    continue;
                }
                checkMap.put(compName, 1);
                poolSizeSB.append(compName + innerSplitter + tdContentList.get(1).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(poolSizeSB, outerSplitter);
        modelingCasesBean.setPool_size(poolSizeSB.toString());
    }

    private void parseFluxNet(String fluxNetContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(fluxNetContents, "<tr>", "</tr>");
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer fluxNetSB = new StringBuffer();
        Map<String, Integer> checkMap = new HashMap<>();
        for(String trContent : trContentList) {
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            if(tdContentList.size() >= 2) {
                String rName = tdContentList.get(0).trim();
                if(checkMap.containsKey(rName)) {
                    continue;
                }
                checkMap.put(rName, 1);
                fluxNetSB.append(rName + innerSplitter + tdContentList.get(1).trim() + outerSplitter);
            }
        }
        StringUtil.removeStringBufferEndTag(fluxNetSB, outerSplitter);
        modelingCasesBean.setFluxes_net(fluxNetSB.toString());
    }

    private void parseReactionContents(String reactionContents, ModelingCasesBean modelingCasesBean) {
        List<String> trContentList = StringUtil.parseContentWithTag(reactionContents, "<tr>", "</tr>");
        String rName = "";
        String innerSplitter = " : ";
        String outerSplitter = " ; ";
        StringBuffer compFormulaSB = new StringBuffer();
        StringBuffer carbonFormulaSB = new StringBuffer();
        StringBuffer compReactantSB = new StringBuffer();
        StringBuffer compProductSB = new StringBuffer();
        StringBuffer carbonReactantSB = new StringBuffer();
        StringBuffer carbonProductSB = new StringBuffer();
        for(int index = 0; index < trContentList.size(); index++) {
            String trContent = trContentList.get(index);
            List<String> tdContentList = StringUtil.parseContentWithTag(trContent, "<td>", "</td>");
            String comp, tag, carbon;
            if(tdContentList.size() >= 4) {
                if(!StringUtil.isBlank(rName)) {
                    StringUtil.removeStringBufferEndTag(" + ", compReactantSB, carbonReactantSB, compProductSB, carbonProductSB);
                    compFormulaSB.append(rName + innerSplitter + compReactantSB.toString() + " --> " + compProductSB.toString() + outerSplitter);
                    carbonFormulaSB.append(rName + innerSplitter + carbonReactantSB.toString() + " --> " + carbonProductSB.toString() + outerSplitter);
                    compReactantSB = new StringBuffer();
                    compProductSB = new StringBuffer();
                    carbonReactantSB = new StringBuffer();
                    carbonProductSB = new StringBuffer();
                }
                rName = tdContentList.get(0).trim();
                rName = StringUtil.moveBeginningNumberToEnd(rName);
                comp = tdContentList.get(1).trim();
                tag = tdContentList.get(2).trim();
                carbon = tdContentList.get(3).trim();
            } else {
                comp = tdContentList.get(0).trim();
                tag = tdContentList.get(1).trim();
                carbon = tdContentList.get(2).trim();
            }
            if(tag.equals("Reactant")) {
                compReactantSB.append(comp + " + ");
                carbonReactantSB.append(carbon + " + ");
            } else if(tag.equals("Product")) {
                compProductSB.append(comp + " + ");
                carbonProductSB.append(carbon + " + ");
            }
        }
        StringUtil.removeStringBufferEndTag(" + ", compReactantSB, carbonReactantSB, compProductSB, carbonProductSB);
        compFormulaSB.append(rName + innerSplitter + compReactantSB.toString() + " --> " + compProductSB.toString());
        carbonFormulaSB.append(rName + innerSplitter + carbonReactantSB.toString() + " --> " + carbonProductSB.toString());
        modelingCasesBean.setComp_formula(compFormulaSB.toString());
        modelingCasesBean.setCarbon_formula(carbonFormulaSB.toString());
    }

    @GetMapping("/reaction_computing_result")
    public String computeResultTip(@RequestParam("timestamp") long timestamp, Model model) {
        model.addAttribute("result_url", HTTPConstant.RESULT_URL_PREFIX + timestamp);
        return "compute_result";
    }

    @GetMapping("/model")
    public String checkComputingResult(@RequestParam(value = "timestamp") long timestamp,
                                       @RequestParam(value = "src") String src,
                                       Model model) {
        ModelingCasesBean modelingCasesBean = modelingService.queryModelingCaseByTimestamp(timestamp);
        if(modelingCasesBean == null) {
            if(src.equals("demo")) {
                model.addAttribute("timestamp", timestamp);
                return "forward:/demo";
            } else {
                model.addAttribute("timestamp", timestamp);
                return "forward:/initializing";
            }
        } else if(modelingCasesBean.getStatus() == 3) {
            return "forward:/modeling_case_detail";
        } else if(modelingCasesBean.getStatus() == 4){
            return "compute_error";
        } else {
            model.addAttribute("src", src);
            model.addAttribute("timestamp", timestamp);
            return "inter_cal_process";
        }
    }

}
