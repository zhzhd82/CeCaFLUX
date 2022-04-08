package edu.gyu.mfa.db.bean;

public class ModelingCase {

    private long id;
    private String email;
    private String name;
    private long timestamp;
    private String comp_formula;
    private String carbon_formula;
    private String fluxes_net;
    private String equalities;
    private String inequalities;
    private String label_input;
    private String mass_spectrometry;
    private int sample_space;
    private String method;
    private String step;
    private String pool_size;
    private int status;
    private String is_public;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFluxes_net() {
        return fluxes_net;
    }

    public void setFluxes_net(String fluxes_net) {
        this.fluxes_net = fluxes_net;
    }

    public String getEqualities() {
        return equalities;
    }

    public void setEqualities(String equalities) {
        this.equalities = equalities;
    }

    public String getInequalities() {
        return inequalities;
    }

    public void setInequalities(String inequalities) {
        this.inequalities = inequalities;
    }

    public String getLabel_input() {
        return label_input;
    }

    public void setLabel_input(String label_input) {
        this.label_input = label_input;
    }

    public String getMass_spectrometry() {
        return mass_spectrometry;
    }

    public void setMass_spectrometry(String mass_spectrometry) {
        this.mass_spectrometry = mass_spectrometry;
    }

    public int getSample_space() {
        return sample_space;
    }

    public void setSample_space(int sample_space) {
        this.sample_space = sample_space;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getPool_size() {
        return pool_size;
    }

    public void setPool_size(String pool_size) {
        this.pool_size = pool_size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComp_formula() {
        return comp_formula;
    }

    public void setComp_formula(String comp_formula) {
        this.comp_formula = comp_formula;
    }

    public String getCarbon_formula() {
        return carbon_formula;
    }

    public void setCarbon_formula(String carbon_formula) {
        this.carbon_formula = carbon_formula;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIs_public() {
        return is_public;
    }

    public void setIs_public(String is_public) {
        this.is_public = is_public;
    }

}
