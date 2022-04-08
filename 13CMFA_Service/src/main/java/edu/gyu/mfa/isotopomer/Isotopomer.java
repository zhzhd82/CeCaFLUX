package edu.gyu.mfa.isotopomer;

import java.util.Arrays;

public class Isotopomer implements Comparable<Isotopomer> {

	//the compound name
	private String name;
	private String carbon;
	private String code;
	private int[] vector;
	private double value;

	public Isotopomer(String name, String carbon, String code, double value) {
		this.name = name;
		this.carbon = carbon;
		this.code = code;
		this.value = value;
		this.vector = IsotopomerTool.generateVectorFromCode(code);
	}

	public Isotopomer(String name, String code, double value) {
		this.name = name;
		this.code = code;
		this.value = value;
		this.vector = IsotopomerTool.generateVectorFromCode(code);
	}

	public Isotopomer(String name, String carbon, int[] vector) {
		this.name = name;
		this.carbon = carbon;
		this.vector = vector;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	public int[] getVector() {
		return vector;
	}

	public String getCarbon() {
		return carbon;
	}

	@Override
	public String toString() {
		return "Isotopomer{" +
				"name='" + name + '\'' +
				", carbon='" + carbon + '\'' +
				", code='" + code + '\'' +
				", vector=" + Arrays.toString(vector) +
				", value=" + value +
				'}';
	}

	@Override
	public int compareTo(Isotopomer o) {
		return code.compareTo(o.getCode());
	}

}
