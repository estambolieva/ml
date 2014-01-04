package com.wordpress.moreintelligent.langdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.LineNumberReader;

import org.ejml.simple.SimpleMatrix;

public class TrainLanguageDetector {

	private static final int NUMBER_CLASSES = 2;
	private static final int NUMBER_TRIGRAMS = 10000000;
	private static final int DEFAULT_NUM_DOCS = 1;
	private static final String SEPARATOR = "/";

	private int n_classes = NUMBER_CLASSES;
	private int n_trigrams = NUMBER_TRIGRAMS;
	private int trigramCount = 0;
	private int numDocs = DEFAULT_NUM_DOCS;
	private String separator = SEPARATOR;

	private List<String> trigramStrings;
	private SimpleMatrix trigramDoc;
	private SimpleMatrix trigramCountPerClass;
	private SimpleMatrix classes, classesDiff;
	private SimpleMatrix prior;

	public TrainLanguageDetector() {
		trigramStrings = new ArrayList<String>();
	}

	public TrainLanguageDetector(int numTrigrams, int numClasses) {
		this.n_trigrams = numTrigrams;
		this.n_classes = numClasses;
		trigramStrings = new ArrayList<String>();
	}

	public TrainLanguageDetector(int numClasses) {
		this.n_classes = numClasses;
		trigramStrings = new ArrayList<String>();
	}

	public void train(String dataLoc, String classLoc) throws IOException {
		checkPathType(dataLoc);
		File[] fileList = getFileList(dataLoc);
		getDocumentCount(fileList);
		MatrixOperations mo = new MatrixOperations();
		readDocClassesToVector(classLoc, mo);
		readFilesFromDir(fileList, dataLoc, mo);
		constructTrigramLangMatrix(dataLoc, mo);
	}

	/*
	 * The paths provided point to a Unix file system if does not contain :\\
	 */
	private void checkPathType(String dataLoc) {
		if (dataLoc.contains(":\\")) {
			separator = "\\";
		} else {
			separator = "/";
		}
	}

	private File[] getFileList(String directory) {
		File dir = new File(directory);
		return dir.listFiles();
	}

	private void getDocumentCount(File[] fList) {
		numDocs = fList.length;
	}

	private void readDocClassesToVector(String cLoc, MatrixOperations mo)
			throws IOException {
		LineNumberReader cReader = new LineNumberReader(new FileReader(
				new File(cLoc)));
		classes = mo.initialiseMatrix(classes, numDocs, 1);
		List<String> seenClasses = new ArrayList<String>();
		String cl;
		int rowNum = 0;
		while ((cl = cReader.readLine()) != null) {
			classes.set(rowNum, 0, Double.parseDouble(cl));
			if (!seenClasses.contains(cl))
				seenClasses.add(cl);
			rowNum += 1;
		}
		classesDiff = mo.initialiseMatrix(classesDiff, seenClasses.size(), 1);
		classesDiff = populateMatrix(seenClasses);
		classes.saveToFileBinary(cLoc.substring(0, cLoc.lastIndexOf(separator))
				+ separator + "class.per.doc");
		classesDiff.saveToFileBinary(cLoc.substring(0, cLoc.lastIndexOf(separator))
				+ separator + "classes");
		n_classes = classesDiff.numRows();
	}

	private SimpleMatrix populateMatrix(List<?> seenClasses) {
		int ind = 0;
		while (ind < seenClasses.size()) {
			classesDiff.set(ind, 0,
					Double.parseDouble((String) seenClasses.get(ind)));
			ind++;
		}
		return classesDiff;
	}

	private void readFilesFromDir(File[] fList, String location,
			MatrixOperations mo) throws IOException {
		trigramDoc = mo.initialiseMatrix(trigramDoc, numDocs, n_trigrams);
		prior = mo.initialiseMatrix(prior, n_classes, 1);
		int row = 0;
		for (File file : fList) {
			if (file.isFile()) {
				addFileDataToMatrix(file.getAbsolutePath(), row);
			}
			row++;
		}
		writeTrigramStringToFile(location.substring(0,
				location.lastIndexOf(separator))
				+ separator + "all.trigrams");
		SimpleMatrix trainingMatrix = getPartialMatrix(row,
				trigramStrings.size());
		prior = prior.divide((double) trigramCount);
		prior.saveToFileBinary(location.substring(0, location.lastIndexOf(separator))
				+ separator + "prior");
		trainingMatrix.saveToFileBinary(location.substring(0,
				location.lastIndexOf(separator))
				+ separator + "training.data");
	}

	private void addFileDataToMatrix(String path, int rowNum)
			throws IOException {
		NgramOperations to = new NgramOperations();
		LineNumberReader lnReader = new LineNumberReader(new FileReader(
				new File(path)));
		String line, trigram;
		int index = 0;
		while ((line = lnReader.readLine()) != null) {
			while (to.hasNextNgram(line, index, 3)) {
				trigram = to.readNgram(line, index, 3);
				addTrigramToMatrix(trigram, rowNum);
				index++;
			}
			// sanity check: regulating number of read tri-grams
			if (trigramCount >= n_trigrams) {
				System.out
						.println("Attention. Too many trigrams read. Terminating loading data");
				break;
			}
		}
	}

	private void addTrigramToMatrix(String term, int rowNum) {
		int columnNum;
		if (!trigramStrings.contains(term)) {
			trigramStrings.add(term);
			columnNum = trigramStrings.indexOf(term);
			trigramDoc.set(rowNum, columnNum, 1.0);
			increaseLangTrigramCount(term, rowNum);
			trigramCount++;
		} else {
			columnNum = trigramStrings.indexOf(term);
			double value = trigramDoc.get(rowNum, columnNum);
			trigramDoc.set(rowNum, columnNum, value + 1.0);
		}
	}

	private void increaseLangTrigramCount(String trigram, int rowNum) {
		double previousCount = prior.get((int) classes.get(rowNum));
		prior.set((int) classes.get(rowNum), previousCount + 1);
	}

	private void writeTrigramStringToFile(String path) {
		try {
			FileOutputStream arrayList_out = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(arrayList_out);
			oos.writeObject(trigramStrings);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private SimpleMatrix getPartialMatrix(int nRows, int nColumns) {
		SimpleMatrix sm = new SimpleMatrix(nRows, nColumns), vector;
		int i = 0;
		while (i < nColumns) {
			vector = trigramDoc.extractVector(false, i);
			int j = 0;
			while (j < nRows) {
				sm.set(j, i, vector.get(j, 0));
				j += 1;
			}
			i += 1;
		}
		return sm;
	}

	private void constructTrigramLangMatrix(String location, MatrixOperations mo)
			throws IOException {
		trigramCountPerClass = mo.initialiseMatrix(trigramCountPerClass,
				n_classes, trigramCount);
		SimpleMatrix trainingMatrix = SimpleMatrix.loadBinary(location
				.substring(0, location.lastIndexOf(separator)) + separator + "training.data");
		int y, pointer = 0, row = 0;
		while (pointer < numDocs) {
			// y - current class
			y = (int) classes.get(pointer);
			if (y == (int) classes.get(pointer)) {
				SimpleMatrix tempTrigramCountVector = trigramCountPerClass
						.extractVector(true, y).plus(
								trainingMatrix.extractVector(true, pointer));
				trigramCountPerClass.setRow(y, 0, mo.vectorToDoubleArray(
						tempTrigramCountVector, trigramCount));
			}
			pointer++;
		}
		trigramCountPerClass.saveToFileBinary(location.substring(0,
				location.lastIndexOf(separator))
				+ separator + "trigram.count.per.class");
	}

	public static void main(String[] args) throws IOException {
		TrainLanguageDetector readData = new TrainLanguageDetector();
		readData.train(args[0], args[1]);
	}

}