package edu.gyu.mfa.reaction;

import edu.gyu.mfa.compound.Compound;
import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.info.Constant;

import java.util.ArrayList;
import java.util.List;

public class ReactionParser {

	public static List<Reaction> parseNetwork(ModelingCase modelingCase) {
		List<Reaction> reactions = new ArrayList<>();

		String[] compFormulaSplits = modelingCase.getComp_formula().split(Constant.OUTER_SPLITTER);
		String[] carbonFormulaSplits = modelingCase.getCarbon_formula().split(Constant.OUTER_SPLITTER);

		for(int index = 0; index < compFormulaSplits.length; index++) {
			Reaction reaction = new Reaction();
			String compFormula = compFormulaSplits[index];
			String carbonFormula = carbonFormulaSplits[index];

			String[] compPartSplits = compFormula.split(Constant.INNER_SPLITTER);
			String[] carbonPartSplits = carbonFormula.split(Constant.INNER_SPLITTER);
			reaction.setName(compPartSplits[0].trim());

			String[] compSplits = compPartSplits[1].split("\\s-->\\s");
			String[] compReactantSplits = compSplits[0].split("\\s\\+\\s");
			String[] compProductSplits = compSplits[1].split("\\s\\+\\s");

			String[] carbonSplits = carbonPartSplits[1].split("\\s-->\\s");
			String[] carbonReactantSplits = carbonSplits[0].split("\\s\\+\\s");
			String[] carbonProductSplits = carbonSplits[1].split("\\s\\+\\s");

			for(int rIndex = 0; rIndex < compReactantSplits.length; rIndex++) {
				reaction.addReactant(new Compound(compReactantSplits[rIndex].trim(), carbonReactantSplits[rIndex].trim()));
			}

			for(int pIndex = 0; pIndex < compProductSplits.length; pIndex++) {
				reaction.addProduct(new Compound(compProductSplits[pIndex].trim(), carbonProductSplits[pIndex].trim()));
			}

			if (reaction.getReactants().size() == 1) {
				reaction.addType(Constant.SINGLE_REACTION);
			} else {
				reaction.addType(Constant.MULTI_REACTION);
			}
			reactions.add(reaction);
		}
		return reactions;
	}
	
}
