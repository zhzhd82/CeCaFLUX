package edu.gyu.mfa.reaction;

import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.matrix.FluxEquationMatrix;
import edu.gyu.mfa.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reaction extends AbstractReaction {

	private List<Compound> reactants;
	private List<Compound> products;
	private Map<String, int[][]> atomTransferMatrixMap;

	public Reaction() {
		reactants = new ArrayList<>();
		products = new ArrayList<>();
		atomTransferMatrixMap = new HashMap<>();
	}

	public int[][] findAtomTransferMatrix(String... names) {
		String key = Util.joinNames(Constant.NAME_SPLITTER, names);
		return atomTransferMatrixMap.get(key);
	}

	public void addAtomTransferMatrix(String name1, String carbon1, String name2, String carbon2, int[][] array) {
		atomTransferMatrixMap.put(Util.joinNames(Constant.NAME_SPLITTER, name1, carbon1, name2, carbon2), array);
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

	public void computeAtomTransferMatrix() {
		for (int rIndex = 0; rIndex < reactants.size(); rIndex++) {
			for (int pIndex = 0; pIndex < products.size(); pIndex++) {
				String[] rCarbonSplits = reactants.get(rIndex).getCarbon().split(Constant.CARBON_SPLITTER);
				String[] pCarbonSplits = products.get(pIndex).getCarbon().split(Constant.CARBON_SPLITTER);
				int[][] array = new int[pCarbonSplits.length][rCarbonSplits.length];
				for (int col = 0; col < rCarbonSplits.length; col++) {
					for (int row = 0; row < pCarbonSplits.length; row++) {
						if (rCarbonSplits[col].equals(pCarbonSplits[row])) {
							array[row][col] = 1;
							break;
						}
					}
				}
				addAtomTransferMatrix(reactants.get(rIndex).getName(), reactants.get(rIndex).getCarbon(),
						products.get(pIndex).getName(), products.get(pIndex).getCarbon(),  array);
			}
		}
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

	public void generateCompoundIsotopomers() {
		for(Compound reactant : reactants) {
			reactant.generateIsotopomers();
		}
		for(Compound product : products) {
			product.generateIsotopomers();
		}
	}

}
