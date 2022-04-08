package cn.gyu.flux.controller;

import cn.gyu.flux.db.bean.GenomeNetworkReactionBean;
import cn.gyu.flux.service.GenomeNetworkReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private GenomeNetworkReactionService genomeNetworkReactionService;

    @PostMapping("/search_genome_reaction")
    public String searchGenomeReaction(@RequestParam(value = "rxn_id", required = false) String rxn_id,
                                       @RequestParam(value = "rxn_name", required = false) String rxn_name,
                                       @RequestParam(value = "compound_name", required = false) String compound,
                                       Model model) {

        if (!StringUtils.isEmpty(rxn_id)) {
            model.addAttribute("search_rxn_id", rxn_id);
        }
        if (!StringUtils.isEmpty(rxn_name)) {
            model.addAttribute("search_rxn_name", rxn_name);
        }
        if (!StringUtils.isEmpty(compound)) {
            model.addAttribute("search_compound_name", compound);
        }

        List<GenomeNetworkReactionBean> list;
        if (!StringUtils.isEmpty(rxn_id) && !StringUtils.isEmpty(rxn_name) && !StringUtils.isEmpty(compound)) {
            list = genomeNetworkReactionService.queryGenomeNetworkReactionByRxnIDNameAndCompound(rxn_id, rxn_name, compound);
        } else if (!StringUtils.isEmpty(rxn_name) && !StringUtils.isEmpty(compound)) {
            list = genomeNetworkReactionService.queryGenomeNetworkReactionByRxnNameAndCompound(rxn_name, compound);
        } else if(!StringUtils.isEmpty(rxn_id)) {
            list = genomeNetworkReactionService.queryGenomeNetworkReactionByRxnID(rxn_id);
        } else if (!StringUtils.isEmpty(rxn_name)) {
            list = genomeNetworkReactionService.queryGenomeNetworkReactionByRxnName(rxn_name);
        } else if (!StringUtils.isEmpty(compound)) {
            list = genomeNetworkReactionService.queryGenomeNetworkReactionByCompound(compound);
        } else {
            list = new ArrayList<>();
        }
        for (GenomeNetworkReactionBean genomeNetworkReactionBean : list) {
            genomeNetworkReactionBean.parseComps();
        }
        model.addAttribute("genome_network_reactions", list);
        model.addAttribute("is_search_result", true);
        if (list.size() == 0) {
            model.addAttribute("no_search_result", "No reaction matched!");
        }
        return "search";
    }

    @PostMapping("/reaction_selected_from_search")
    public String selectReactionFromSearchResults(@RequestParam("select_reaction_rxn_id") String rxnIdSelected, Model model) {
        List<GenomeNetworkReactionBean> reactionSelectedList = new ArrayList<>();
        if (!StringUtils.isEmpty(rxnIdSelected)) {
            String[] splits = rxnIdSelected.split(",");
            Map<String, Integer> checkMap = new HashMap<>();
            for (String split : splits) {
                if (!StringUtils.isEmpty(split)) {
                    checkMap.put(split, 1);
                }
            }
            List<GenomeNetworkReactionBean> reactions = genomeNetworkReactionService.queryAllGenomeNetworkReactions();
            for (GenomeNetworkReactionBean bean : reactions) {
                if (checkMap.containsKey(bean.getRxn_id())) {
                    bean.parseComps();
                    reactionSelectedList.add(bean);
                }
            }
        }
        model.addAttribute("selected_reaction_from_search", reactionSelectedList);
        return "model";
    }

}
