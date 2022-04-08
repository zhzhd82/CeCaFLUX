package edu.gyu.mfa.reaction;

public abstract class AbstractReaction {
    protected String name;
    protected String type;
    protected Flux flux;

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

    public void addType(String _type) {
        if (type == null) {
            type = _type;
        } else {
            type += _type;
        }
    }

    public Flux getFlux() {
        return flux;
    }

    public void setFlux(Flux _flux) {
        flux = _flux;
    }

    public void setFlux(double value) {
        if(flux == null) {
            setFlux(new Flux(value));
        } else {
            flux.setValue(value);
        }
    }

    public void setConsed(boolean consed) {
        if(flux == null) {
            setFlux(new Flux(consed));
        } else {
            flux.setConsed(consed);
        }
    }

    public void setIndex(int index) {
        if(flux == null) {
            setFlux(new Flux(index));
        } else {
            flux.setIndex(index);
        }
    }
}
