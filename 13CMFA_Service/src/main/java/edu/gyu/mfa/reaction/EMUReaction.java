package edu.gyu.mfa.reaction;

import edu.gyu.mfa.emu.EMU;
import edu.gyu.mfa.info.Constant;
import java.util.ArrayList;
import java.util.List;

public class EMUReaction extends AbstractReaction{

	private List<EMU> reactants;
	private List<EMU> products;
	private int maxSize;

	public EMUReaction() {
		reactants = new ArrayList<>();
		products = new ArrayList<>();
	}

	public void addReactant(EMU reacatant) {
		reactants.add(reacatant);
	}

	public void addProduct(EMU product) {
		products.add(product);
	}

	public List<EMU> getReactants() {
		return reactants;
	}

	public EMU getReactant(int index) {
		return reactants.get(index);
	}

	public EMU getProduct(int index) {
		return products.get(index);
	}

	public List<EMU> getProducts() {
		return products;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxReactantSize() {
		int max = 0;
		int rSize;
		for(EMU reactant : reactants) {
			rSize = reactant.getSize();
			if(rSize > max) {
				max = rSize;
			}
		}
		return max;
	}

	public int getMaxProductSize() {
		int max = 0;
		int pSize;
		for(EMU product : products) {
			pSize = product.getSize();
			if(pSize > max) {
				max = pSize;
			}
		}
		return max;
	}

	public void computeMaxMass() {
		int rMax = getMaxReactantSize();
		int pMax = getMaxProductSize();
		setMaxSize(rMax > pMax ? rMax : pMax);
	}

	public void parseType(String rType) {
		type = null;
		if(reactants.size() == 1) {
			addType(Constant.SINGLE_REACTION);
		} else {
			addType(Constant.MULTI_REACTION);
		}

		if(rType.contains(Constant.INPUT_REACTION)) {
			addType(Constant.INPUT_REACTION);
		} else if(rType.contains(Constant.INTER_REACTION)) {
			addType(Constant.INTER_REACTION);
		} else {
			addType(Constant.OUTPUT_REACTION);
		}
	}

}
