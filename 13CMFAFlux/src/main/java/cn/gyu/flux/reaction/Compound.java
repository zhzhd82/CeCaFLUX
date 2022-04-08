package cn.gyu.flux.reaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compound implements Comparable<Compound> {

	private String name;
	private String carbon;

	public Compound(String name, String carbon) {
		this.name = name;
		this.carbon = carbon;
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

	@Override
	public int compareTo(Compound o) {
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return "Compound [name=" + name + ", carbon=" + carbon + "]";
	}
	
}
