package cn.gyu.flux.reaction;

public abstract class AbstractReaction {
    protected String name;
    protected String type;

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getType() {
        return type;
    }

    public void setType(String _type) {
        type = _type;
    }

}
