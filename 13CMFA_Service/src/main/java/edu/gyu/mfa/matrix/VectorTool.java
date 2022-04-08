package edu.gyu.mfa.matrix;

import edu.gyu.mfa.info.Constant;
import edu.gyu.mfa.info.Count;
import edu.gyu.mfa.info.Norm2ValueFluxX;
import edu.gyu.mfa.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VectorTool {

	public static int parseWeightFromVector(int[] vector) {
		int weight = 0;
		for(int index=0; index < vector.length; index++) {
			if(vector[index] == 1) {
				weight++;
			}
		}
		return weight;
	}

	public static double[] getSubVetor(double[] vector, int start, int end) {
		double[]  result = new double[end - start];
		int rIndex = 0;
		for(int index = start; index < end; index++) {
			result[rIndex++] = vector[index];
		}
		return result;
	}

	public static int[] addVector(int[] vector1, int[] vector2) {
		if(vector1.length != vector2.length) {
			return null;
		}
		int[] result = new int[vector1.length];
		for(int index = 0; index < vector1.length; index++) {
			result[index] = vector1[index] + vector2[index];
		}
		return result;
	}

	public static double[] addVector(double[] vector1, double[] vector2) {
		if(vector1.length != vector2.length) {
			return null;
		}
		double[] result = new double[vector1.length];
		for(int index = 0; index < vector1.length; index++) {
			result[index] = vector1[index] + vector2[index];
		}
		return result;
	}

	public static double[] subVector(double[] vector1, double[] vector2) {
		if(vector1.length != vector2.length) {
			return null;
		}
		int len = vector1.length;
		double[] result = new double[len];
		for(int index = 0; index < len; index++) {
			result[index] = vector1[index] - vector2[index];
		}
		return result;
	}

	public static double[][] convertVectorToColArray(double[] vector) {
		int numRows = vector.length;
		double[][] result = new double[numRows][1];
		for(int row = 0; row < numRows; row++) {
			result[row][0] = vector[row];
		}
		return result;
	}

	public static double[] generateZeroVector(int size) {
		double[] result = new double[size];
		for(int index = 0; index < size; index++) {
			result[index] = 0.0;
		}
		return result;
	}

	public static double[] generateRandomVector(int size, double minValue, double maxValue) {
		double[] result = new double[size];
		for(int index = 0; index < size; index++) {
			result[index] = Util.nextDouble(minValue, maxValue);
		}
		return result;
	}

	public static double[] convertListToVector(List<Double> xList) {
		double[] X = new double[xList.size()];
		for (int index = 0; index < xList.size(); index++) {
			X[index] = xList.get(index);
		}
		return X;
	}

	public static double[] vectorTimesNumber(double[] vector, double number) {
		double[] result = new double[vector.length];
		for(int index = 0; index < result.length; index++) {
			result[index] = vector[index] * number;
		}
		return result;
	}

	public static double vectorTimesVector(double[] vector1, double[] vector2) {
		double result = 0;
		for(int index = 0; index < vector1.length; index++) {
			result += vector1[index] * vector2[index];
		}
		return result;
	}

	public static double[] appendVector(double[] Fk, double[] F) {
		if(F == null) {
			return Fk;
		}

		if(Fk == null) {
			return F;
		}

		double[] result = new double[F.length + Fk.length];
		for(int index = 0; index < result.length; index++) {
			if(index < F.length) {
				result[index] = F[index];
			} else {
				result[index] = Fk[index - F.length];
			}
		}
		return result;
	}

	public static double[] computeUBVector() {
		double[] ub = new double[Count.freeCount + Count.cFreeCount];
		for(int index = 0; index < Count.freeCount + Count.cFreeCount; index++) {
			if(index < Count.freeCount) {
				ub[index] = Constant.V_UB;
			} else {
				ub[index] = Constant.C_UB;
			}
		}
		return ub;
	}

	public static double[] computeLBVector() {
		double[] lb = new double[Count.freeCount + Count.cFreeCount];
		for(int index = 0; index < Count.freeCount + Count.cFreeCount; index++) {
			if(index < Count.freeCount) {
				lb[index] = Constant.V_LB;
			} else {
				lb[index] = Constant.C_LB;
			}
		}
		return lb;
	}

	public static double[] deepCopyVector(double[] vector) {
		double[] result = new double[vector.length];
		for(int index = 0; index < vector.length; index++) {
			result[index] = vector[index];
		}
		return result;
	}

	public static double computeMeanValue(List<Norm2ValueFluxX> norm2ValueFluxXList) {
		int number = norm2ValueFluxXList.size();
		double sum = 0;
		for(Norm2ValueFluxX norm2ValueFluxX : norm2ValueFluxXList) {
			sum += norm2ValueFluxX.getNorm2_value();
		}
		return sum / number;
	}

	public static List<Integer> sampleArrayIndexRandom(double[] data, int k) {
		List<Integer> result = new ArrayList<>();
		List<Integer> indexList = new ArrayList<>();
		for(int index = 0; index < data.length; index++) {
			indexList.add(index);
		}
		if(k >= data.length) {
			result = indexList;
		} else {
			Random random = new Random();
			for(int count = 0; count < k; count++) {
				result.add(indexList.remove(random.nextInt(indexList.size())));
			}
		}
		return result;
	}

	public static double[] formatVector(double[] vector) {
		double[] result = new double[vector.length];
		for(int index = 0; index < result.length; index++) {
			result[index] = Util.formatNumber(vector[index]);
		}
		return result;
	}

	public static double[] computeDeviation(double[] upper_bound, double[] lower_bound) {
		int len = upper_bound.length;
		double[] result = new double[len];
		for(int index = 0; index < len; index++) {
			result[index] = (upper_bound[index] - lower_bound[index]) / 2;
		}
		return result;
	}

}
