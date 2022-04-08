package edu.gyu.mfa.isotopomer;

public class IsotopomerTool {

	public static int parseWeightFromCode(String code) {
		int weight = 0;
		String[] splits = code.split("");
		for (String split : splits) {
			if (split.equals("1")) {
				weight++;
			}
		}
		return weight;
	}
	
	public static int[] generateVectorFromCode(String code) {
		int[] vector = new int[code.length()];
		String[] splits = code.split("");
		for(int index = 0; index < splits.length; index++) {
			vector[index] = Integer.parseInt(splits[index]);
		}
		return vector;
	}
	
	public static double computeIsotopomerDefaultFraction(String code) {
		int weight = parseWeightFromCode(code);
		return Math.pow(0.011, weight) * Math.pow(0.989, code.length()-weight);
	}

}
