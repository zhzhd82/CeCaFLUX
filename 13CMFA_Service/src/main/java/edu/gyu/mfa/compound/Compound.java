package edu.gyu.mfa.compound;

import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.isotopomer.Isotopomer;
import edu.gyu.mfa.isotopomer.IsotopomerTool;
import edu.gyu.mfa.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compound implements Comparable<Compound> {

	private String name;
	private String carbon;
	private List<Isotopomer> isotopomers;

	public Compound(String name, String carbon) {
		this.name = name;
		this.carbon = carbon;
		isotopomers = new ArrayList<>();
	}

	public List<Isotopomer> getIsotopomers() {
		return isotopomers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCarbon() {
		return carbon;
	}

	public void generateIsotopomers() {
		if(isotopomers.size() > 0) {
			return;
		}
		int carbonLen = carbon.split(Constant.CARBON_SPLITTER).length;
		for (long value = 0; value <= (long) Math.pow(2, carbonLen) - 1; value++) {
			String code = Util.paddingPrefixWithZero(Long.toBinaryString(value), carbonLen);
			isotopomers.add(new Isotopomer(name, carbon, code, IsotopomerTool.computeIsotopomerDefaultFraction(code)));
		}
		Collections.sort(isotopomers);
	}

	@Override
	public int compareTo(Compound o) {
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return "Compound [name=" + name + ", carbon=" + carbon + "]";
	}
	
}
