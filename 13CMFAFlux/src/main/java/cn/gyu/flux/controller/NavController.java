package cn.gyu.flux.controller;

import cn.gyu.flux.db.bean.GenomeNetworkReactionBean;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.service.GenomeNetworkReactionService;
import cn.gyu.flux.service.ModelingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class NavController {

    @Autowired
    private GenomeNetworkReactionService genomeNetworkReactionService;

    @Autowired
    private ModelingService modelingService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/cases")
    public String getCases(Model model) {
        List<ModelingCasesBean> list = modelingService.queryAllModelingCases();
        model.addAttribute("modeling_cases_list", list);
        return "cases";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }

    @GetMapping("/genome_network_reaction")
    public String getGenomeNetworkReactions(Model model) {
        setGenomeNetworkReactionParams(1, model);
        return "genome_network_reaction";
    }

    @GetMapping("/initializing")
    public String model(Model model) {
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "model";
    }

    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "demo";
    }

    @GetMapping("/large_network_demo")
    public String largeDemo(Model model) {
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "large_network_demo";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @PostMapping("/genome_network_reaction_page")
    public String getGenomeNetworkReactionsByPage(@RequestParam(value = "page") Integer page, Model model) {
        setGenomeNetworkReactionParams(page, model);
        return "genome_network_reaction";
    }

    private void setGenomeNetworkReactionParams(int page, Model model) {
        int pageSize = 15;
        int totalCount = genomeNetworkReactionService.queryTotalCount();
        int lastPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        List<GenomeNetworkReactionBean> genomeNetworkReactionBeanList = genomeNetworkReactionService.queryGenomeNetworkReactions(page, pageSize);
        for(GenomeNetworkReactionBean genomeNetworkReactionBean : genomeNetworkReactionBeanList) {
            genomeNetworkReactionBean.parseComps();
        }
        model.addAttribute("genome_network_reactions", genomeNetworkReactionBeanList);
        model.addAttribute("total_count", totalCount);
        model.addAttribute("cur_page", page);
        model.addAttribute("page_size", pageSize);
        model.addAttribute("last_page", lastPage);
    }
}
