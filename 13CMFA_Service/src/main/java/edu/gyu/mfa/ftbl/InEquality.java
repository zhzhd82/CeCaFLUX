package edu.gyu.mfa.ftbl;

public class InEquality {
    private String reaction_name;
    private String lb_str;
    private String ub_str;

    public InEquality(){}

    public InEquality(String _reaction_name, String _lb_str, String _ub_str) {
        reaction_name = _reaction_name;
        lb_str = _lb_str;
        ub_str = _ub_str;
    }

    public String getReaction_name() {
        return reaction_name;
    }

    public void setReaction_name(String reaction_name) {
        this.reaction_name = reaction_name;
    }

    public String getLb_str() {
        return lb_str;
    }

    public void setLb_str(String lb_str) {
        this.lb_str = lb_str;
    }

    public String getUb_str() {
        return ub_str;
    }

    public void setUb_str(String ub_str) {
        this.ub_str = ub_str;
    }

    public String getInEqualityStr() {
        return reaction_name + "," + lb_str + "," + ub_str;
    }

}
