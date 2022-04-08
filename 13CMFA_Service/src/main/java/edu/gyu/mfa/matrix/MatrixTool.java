package edu.gyu.mfa.matrix;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatrixTool {

	public static int[] matrixTimesVector(int[][] matrix, int[] vector) {
		int[] result = new int[matrix.length];
		for (int row = 0; row < matrix.length; row++) {
			int sum = 0;
			for (int col = 0; col < matrix[0].length; col++) {
				sum += matrix[row][col] * vector[col];
			}
			result[row] = sum;
		}
		return result;
	}

	public static double[] matrixTimesVector(double[][] matrix, double[] vector) {
		double[] result = new double[matrix.length];
		for (int row = 0; row < matrix.length; row++) {
			double sum = 0;
			for (int col = 0; col < matrix[0].length; col++) {
				sum += matrix[row][col] * vector[col];
			}
			result[row] = sum;
		}
		return result;
	}

	public static double[][] rref(double[][] srcMat) {
		int numRows = srcMat.length;
		int numCols = srcMat[0].length;
		double[][] matrix = new double[numRows][numCols];
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				matrix[row][col] = srcMat[row][col];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			int colIndex = nonZeroCol(matrix, i, matrix.length - 1);
			if (colIndex >= 0.0f) {
				if (matrix[i][colIndex] == 0.0f) {
					int j = nextNonZeroRow(matrix, i, colIndex);
					rowSwap(matrix, i, j);
				}
				double pivot = matrix[i][colIndex];

				if (pivot != 1.0f && !closeEnough(pivot, 0.0f)) {
					pivot = 1.0f / matrix[i][colIndex];
					for (int k = 0; k < matrix[0].length; k++) {
						double newValue = matrix[i][k] * pivot;
						if (closeEnough(newValue, Math.round(newValue))) {
							matrix[i][k] = Math.round(newValue);
						} else {
							matrix[i][k] = newValue;
						}
					}
				}

				for (int k = (i + 1); k < matrix.length; k++) {
					double value = matrix[k][colIndex];
					if (value != 0.0f) {
						double first = matrix[k][colIndex];
						for (int el = 0; el < (matrix[0].length); el++) {
							double newValue = -first * matrix[i][el] + matrix[k][el];
							if (closeEnough(newValue, Math.round(newValue))) {
								matrix[k][el] = Math.round(newValue);
							} else {
								matrix[k][el] = newValue;
							}
						}
					}
				}
			}
		}

		int lastNonZeroRow = lastNonZeroRow(matrix);
		for (int k = lastNonZeroRow; k > 0; k--) {
			int firstNonZeroCol = nonZeroCol(matrix, k, k);
			for (int i = 0; i < k; i++) {
				double value = matrix[i][firstNonZeroCol];
				if (value != 0.0f) {
					double first = matrix[i][firstNonZeroCol];
					for (int el = 0; el < (matrix[0].length); el++) {
						double newValue = -first * matrix[k][el] + matrix[i][el];
						if (closeEnough(newValue, Math.round(newValue))) {
							matrix[i][el] = Math.round(newValue);
						} else {
							matrix[i][el] = newValue;
						}
					}
				}
			}
		}
		return matrix;
	}

	private static boolean closeEnough(double pivot, double limit) {
		return (Math.abs(limit - pivot) < 0.001f);
	}

	private static int nonZeroCol(double[][] matrix, int minRow, int maxRow) {
		for (int i = 0; i < matrix[0].length; i++) {
			for (int j = minRow; j <= maxRow; j++) {
				if (matrix[j][i] != 0.0f) {
					return i;
				}
			}
		}
		return -1;
	}

	private static int nextNonZeroRow(double[][] matrix, int rowIndex, int colIndex) {
		for (int i = (rowIndex + 1); i < matrix.length; i++) {
			if (matrix[i][colIndex] != 0.0f) {
				return i;
			}
		}
		return -1;
	}

	private static int lastNonZeroRow(double[][] matrix) {
		boolean zeroRow = true;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] != 0.0f) {
					zeroRow = false;
				}
			}
			if (zeroRow == true) {
				return (i - 1);
			} else if (i == (matrix.length - 1)) {
				return i;
			}
			zeroRow = true;
		}
		return -1;
	}

	private static void rowSwap(double[][] matrix, int first, int second) {
		for (int i = 0; i < matrix[0].length; i++) {
			double tmp = matrix[first][i];
			matrix[first][i] = matrix[second][i];
			matrix[second][i] = tmp;
		}
	}

	public static boolean isZeroRow(double[][] matrix, int row) {
		boolean result = true;
		int numCols = matrix[row].length;
		for (int col = 0; col < numCols; col++) {
			if (matrix[row][col] != 0.0) {
				result = false;
				break;
			}
		}
		return result;
	}

	public static void addColToList(double[][] matrix, int col, List<List<Double>> list) {
		int numRows = matrix.length;
		List<Double> tmp = new ArrayList<>();
		for (int row = 0; row < numRows; row++) {
			tmp.add(matrix[row][col]);
		}
		list.add(tmp);
	}

	public static double[][] convertListToColArray(List<List<Double>> list) {
		int numRows = list.get(0).size();
		int numCols = list.size();
		double[][] result = new double[numRows][numCols];
		for (int col = 0; col < numCols; col++) {
			List<Double> element = list.get(col);
			for (int row = 0; row < element.size(); row++) {
				result[row][col] = element.get(row);
			}
		}
		return result;
	}

	public static MatrixConsFreeInfo computeMatrixConsFree(double[][] matrixForRREF, double[][] matrix) {
		MatrixConsFreeInfo info = new MatrixConsFreeInfo();
		info.setsArray(matrix);
		if (matrixForRREF == null) {
			matrixForRREF = matrix;
		}
		double[][] stoichiometricMatrixRREF = rref(matrixForRREF);
		int numRows = stoichiometricMatrixRREF.length;
		int numCols = stoichiometricMatrixRREF[0].length;
		int rowCons = 0;
		for (int row = 0; row < numRows; row++) {
			if (isZeroRow(stoichiometricMatrixRREF, row)) {
				rowCons = row - 1;
				break;
			}
		}
		if (rowCons == 0) {
			rowCons = numRows - 1;
		}
		List<List<Double>> sConsList = new ArrayList<>();
		List<List<Double>> sFreeList = new ArrayList<>();
		Map<Integer, Integer> sConsMap = new HashMap<>();
		for (int row = 0; row <= rowCons; row++) {
			for (int col = 0; col < numCols; col++) {
				if (stoichiometricMatrixRREF[row][col] != 0.0) {
					addColToList(matrix, col, sConsList);
					sConsMap.put(col, 1);
					break;
				}
			}
		}
		for (int col = 0; col < numCols; col++) {
			if (!sConsMap.containsKey(col)) {
				addColToList(matrix, col, sFreeList);
			}
		}
		double[][] sConsArray = convertListToColArray(sConsList);
		double[][] sFreeArray = convertListToColArray(sFreeList);
		info.setsConsArray(sConsArray);
		info.setsFreeArray(sFreeArray);
		info.setsConsMap(sConsMap);
		return info;
	}

	public static boolean checkByRank(double[][] mat1, double[][] mat2) {
		boolean result = true;
		Matrix matrix1 = new Matrix(mat1);
		Matrix matrix2 = new Matrix(mat2);
		int rank1 = matrix1.rank();
		int rank2 = matrix2.rank();
		if (rank1 != rank2) {
			result = false;
		}
		return result;
	}
	
	public static double[] getMatrixCol(double[][] matrix, int col) {
		int rowNum = matrix.length;
		double[] result = new double[rowNum];
		for(int row = 0; row < rowNum; row++) {
			result[row] = matrix[row][col];
		}
		return result;
	}

	public static double[][] computeB(int length) {
        double[][] B = new double[][] {{1,0},{0,1}};
        for(int i = 2; i<= length; i++) {
            int rowNum = B.length + 1;
            int colNum = B[0].length * 2;
            double[][] Bi = new double[rowNum][colNum];
            for(int row = 0; row < rowNum; row++) {
                for(int col = 0; col < colNum; col++) {
                    if(row < rowNum - 1 && col < colNum / 2) {
                        Bi[row][col] = B[row][col];
                    }

                    if(row == rowNum - 1 && col < colNum / 2) {
                        Bi[row][col] = 0;
                    }

                    if(row == 0 && col >= colNum / 2) {
                        Bi[row][col] = 0;
                    }

                    if(row > 0 && col >= colNum / 2) {
                        Bi[row][col] = B[row - 1][col - colNum / 2];
                    }
                }
            }
            B = Bi;
        }
        return B;
    }

	public static double[][] convertVectorToRowMatrix(double[] vector) {
		int col = vector.length;
		double[][] result = new double[1][col];
		for(int index = 0; index < col; index++) {
			result[0][index] = vector[index];
		}
		return result;
	}

	public static double[][] convertVectorToColMatrix(double[] vector) {
		int rowNum = vector.length;
		double[][] result = new double[rowNum][1];
		for(int row = 0; row < rowNum; row++) {
			result[row][0] = vector[row];
		}
		return result;
	}

	public static double[] getMatrixRow(double[][] matrix, int row) {
		int colNum = matrix[0].length;
		double[] result = new double[colNum];
		for(int col = 0; col < colNum; col++) {
			result[col] = matrix[row][col];
		}
		return result;
	}

	public static double[][] concatenateMatrix(double[][] matk, double[][] mat) {
		if(matk == null) {
			return mat;
		}
		if(mat == null) {
			return matk;
		}
		int rowNum = matk.length;
		int colNum1 = mat[0].length;
		int colNum2 = matk[0].length;
		int colNum = colNum1 + colNum2;
		double[][] result = new double[rowNum][colNum];
		for(int row = 0; row < rowNum; row++) {
			for(int col = 0; col < colNum; col++) {
				if(col < colNum1) {
					result[row][col] = mat[row][col];
				} else {
					result[row][col] = matk[row][col - colNum1];
				}
			}
		}
		return result;
	}

	public static Matrix calculateInverseMatrix(double[][] H) {
		Matrix matH = new Matrix(H);
		int rank = matH.rank();
		Matrix inverseMatH;
		if(rank < H.length) {
			SingularValueDecomposition svd = new Matrix(H).svd();
			double[][] U = svd.getU().getArray();
			double[][] V = svd.getV().getArray();
			double[][] Sigma = svd.getS().getArray();
			int rowNum = Sigma.length;
			double[][] Sigma_1 = new double[rowNum][rowNum];
			for(int row = 0; row < rowNum; row++) {
				if(Sigma[row][row] != 0) {
					Sigma_1[row][row] = 1 / Sigma[row][row];
				}
			}
			inverseMatH = new Matrix(V).times(new Matrix(Sigma_1)).times(new Matrix(U));
		} else {
			inverseMatH = matH.inverse();
		}

		return inverseMatH;
	}

}
