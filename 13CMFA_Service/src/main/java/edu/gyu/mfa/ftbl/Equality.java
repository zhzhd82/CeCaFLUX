package edu.gyu.mfa.ftbl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Equality {

	private double value;
	private Map<String, Double> nameCoefficientMap;

	public Equality() {
		nameCoefficientMap = new HashMap<>();
	}
	
	public Equality(double value, String name, double coefficient) {
		this();
		nameCoefficientMap.put(name, coefficient);
		this.value = value;
	}

	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	public Map<String, Double> getNameCoefficientMap() {
		return nameCoefficientMap;
	}

	public void addNameCoefficient(String name, double coefficient) {
		this.nameCoefficientMap.put(name, coefficient);
	}

	public Set<String> getKeySet() {
		return nameCoefficientMap.keySet();
	}

	public double getCoefficient(String name) {
		return nameCoefficientMap.get(name);
	}
}
