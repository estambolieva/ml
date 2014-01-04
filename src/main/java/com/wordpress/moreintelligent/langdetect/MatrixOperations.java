package com.wordpress.moreintelligent.langdetect;

import org.ejml.simple.SimpleMatrix;

public class MatrixOperations {
	
	/*
	 * initialise the trigram doc matrix; num of documents is the num of rows
	 * and num of words is the nums of columns (int)
	 */
	public SimpleMatrix initialiseMatrix(SimpleMatrix A, int rows, int columns) {
		A = new SimpleMatrix(rows, columns);
		A.zero();
		return A;
	}
	
	protected double[] vectorToDoubleArray(SimpleMatrix tempTrigramCountVector, int count) {
		double[] arr = new double[count];
		int tmp = 0;
		while (tmp < count) {
			arr[tmp] = tempTrigramCountVector.get(tmp);
			tmp++;
		}
		return arr;
	}

}