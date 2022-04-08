package cn.gyu.flux.reaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Reaction extends AbstractReaction {

	private List<Compound> reactants;
	private List<Compound> products;

	public Reaction() {
		reactants = new ArrayList<>();
		products = new ArrayList<>();
	}

	public void addReactant(Compound reacatant) {
		reactants.add(reacatant);
	}

	public void addProduct(Compound product) {
		products.add(product);
	}

	public List<Compound> getReactants() {
		return reactants;
	}

	public List<Compound> getProducts() {
		return products;
	}

	public Compound getProduct(int index) {
		return products.get(index);
	}

	public Compound getReactant(int index) {
		return reactants.get(index);
	}

	public void parseType(FluxEquationMatrix fluxEquationMatrix) {
		if (getReactants().size() == 1) {
			if(parseTypeFlag(fluxEquationMatrix, getReactant(0).getName(), 0, ">")) {
				addType(Constant.INPUT_REACTION);
				return;
			}
		}
		if (getProducts().size() == 1) {
			if (parseTypeFlag(fluxEquationMatrix, getProduct(0).getName(), 0, "<")) {
				addType(Constant.OUTPUT_REACTION);
				return;
			}
		}
		addType(Constant.INTER_REACTION);
	}

	private boolean parseTypeFlag(FluxEquationMatrix fluxEquationMatrix, String name, int value, String operator) {
		boolean flag = true;
		Map<String, Integer> compoundPositionMap = fluxEquationMatrix.getCompoundPositionMap();
		Map<String, Integer> reactionPositionMap = fluxEquationMatrix.getReactionPositionMap();
		double[][] stoichiometricMatrix = fluxEquationMatrix.getStoichiometricMatrix();
		int row = compoundPositionMap.get(name);
		for (int col = 0; col < reactionPositionMap.size(); col++) {
			if(operator.equals(">")) {
				if (stoichiometricMatrix[row][col] > value) {
					flag = false;
					break;
				}
			} else {
				if (stoichiometricMatrix[row][col] < value) {
					flag = false;
					break;
				}
			}

		}
		return flag;
	}

	public void addType(String _type) {
		if (type == null) {
			type = _type;
		} else {
			type += _type;
		}
	}

	public Reaction generateReversedReaction() {
		Reaction rReaction = new Reaction();
		rReaction.setName(name + Constant.REVERSED_REACTION_NAME_SUFFIX);
		for (Compound reactant : reactants) {
			rReaction.addProduct(new Compound(reactant.getName(),reactant.getCarbon()));
		}
		for (Compound product : products) {
			rReaction.addReactant(new Compound(product.getName(),product.getCarbon()));
		}
		if (rReaction.getReactants().size() == 1) {
			rReaction.addType(Constant.SINGLE_REACTION);
		} else {
			rReaction.addType(Constant.MULTI_REACTION);
		}
		return rReaction;
	}

	public int getCompoundCount() {
		return reactants.size() + products.size();
	}

}
