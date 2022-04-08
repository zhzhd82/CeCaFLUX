package cn.gyu.flux.reaction;

public abstract class AbsConsNameValue {

    protected String reaction_name;
    protected double value;

    public AbsConsNameValue(){}

    public AbsConsNameValue(String _reaction_name, double _value) {
        reaction_name = _reaction_name;
        value = _value;
    }

    public String getReaction_name() {
        return reaction_name;
    }

    public void setReaction_name(String reaction_name) {
        this.reaction_name = reaction_name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
