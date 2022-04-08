package edu.gyu.mfa.matrix;

import java.util.HashMap;
import java.util.Map;

public class MatrixConsFreeInfo {
	
	private double[][] sArray;
	private double[][] sConsArray;
	private double[][] sFreeArray;
	private Map<Integer, Integer> sConsMap;
	
	public MatrixConsFreeInfo(){
		sConsMap = new HashMap<>();
	}

	public double[][] getsArray() {
		return sArray;
	}

	public void setsArray(double[][] sArray) {
		this.sArray = sArray;
	}

	public double[][] getsConsArray() {
		return sConsArray;
	}

	public void setsConsArray(double[][] sConsArray) {
		this.sConsArray = sConsArray;
	}

	public double[][] getsFreeArray() {
		return sFreeArray;
	}

	public void setsFreeArray(double[][] sFreeArray) {
		this.sFreeArray = sFreeArray;
	}

	public Map<Integer, Integer> getsConsMap() {
		return sConsMap;
	}

	public void setsConsMap(Map<Integer, Integer> sConsMap) {
		this.sConsMap = sConsMap;
	}

}
