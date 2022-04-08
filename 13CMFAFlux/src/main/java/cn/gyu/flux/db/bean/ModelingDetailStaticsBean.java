package cn.gyu.flux.db.bean;

public class ModelingDetailStaticsBean {

    private int reaction_count;
    private int compound_count;
    private double minimal_norm2;
    private String goodness_of_fit;

    public int getReaction_count() {
        return reaction_count;
    }

    public void setReaction_count(int reaction_count) {
        this.reaction_count = reaction_count;
    }

    public int getCompound_count() {
        return compound_count;
    }

    public void setCompound_count(int compound_count) {
        this.compound_count = compound_count;
    }

    public double getMinimal_norm2() {
        return minimal_norm2;
    }

    public void setMinimal_norm2(double minimal_norm2) {
        this.minimal_norm2 = minimal_norm2;
    }

    public String getGoodness_of_fit() {
        return goodness_of_fit;
    }

    public void setGoodness_of_fit(String goodness_of_fit) {
        this.goodness_of_fit = goodness_of_fit;
    }
}
