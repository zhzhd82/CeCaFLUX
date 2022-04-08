package cn.gyu.flux.db.bean;

import java.util.ArrayList;
import java.util.List;

public class GenomeNetworkReactionBean {

    private Integer id;
    private String rxn_id;
    private String rxn_name;
    private String compound_formula;
    private String carbon_formula;

    private List<CarbonNameComp> comps;
    private int comp_size;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRxn_id() {
        return rxn_id;
    }

    public void setRxn_id(String rxn_id) {
        this.rxn_id = rxn_id;
    }

    public String getRxn_name() {
        return rxn_name;
    }

    public void setRxn_name(String rxn_name) {
        this.rxn_name = rxn_name;
    }

    public String getCompound_formula() {
        return compound_formula;
    }

    public void setCompound_formula(String compound_formula) {
        this.compound_formula = compound_formula;
    }

    public String getCarbon_formula() {
        return carbon_formula;
    }

    public void setCarbon_formula(String carbon_formula) {
        this.carbon_formula = carbon_formula;
    }

    public List<CarbonNameComp> getComps() {
        return comps;
    }

    public void setComps(List<CarbonNameComp> comps) {
        this.comps = comps;
    }

    public int getComp_size() {
        return comp_size;
    }

    public void setComp_size(int comp_size) {
        this.comp_size = comp_size;
    }

    public void parseComps() {
        comps = new ArrayList<>();
        String[] compSplits = compound_formula.split("\\s-->\\s");
        String[] carbonSplits = carbon_formula.split("\\s-->\\s");

        String[] reactantCompSplits = compSplits[0].split("\\s\\+\\s");
        String[] productCompSplits = compSplits[1].split("\\s\\+\\s");
        String[] reactantCarbonSplits = carbonSplits[0].split("\\s\\+\\s");
        String[] productCarbonSplits = carbonSplits[1].split("\\s\\+\\s");

        for(int index = 0; index < reactantCompSplits.length; index++) {
            String rCompName = reactantCompSplits[index].trim();
            String rCarbon = reactantCarbonSplits[index].trim();
            CarbonNameComp carbonNameComp = new CarbonNameComp(rCarbon, rCompName, true);
            comps.add(carbonNameComp);
        }

        for(int index = 0; index < productCompSplits.length; index++) {
            String pCompName = productCompSplits[index].trim();
            String pCarbon = productCarbonSplits[index].trim();
            CarbonNameComp carbonNameComp = new CarbonNameComp(pCarbon, pCompName, false);
            comps.add(carbonNameComp);
        }
        comp_size = comps.size();
    }
}
