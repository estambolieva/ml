package com.wordpress.moreintelligent.langdetect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

public class LanguageDetector {

	private final static int NUM_CLASSES = 2;

	private int n_classes = NUM_CLASSES;
	private SimpleMatrix likelihood;
	private SimpleMatrix prior;

	List<String> trigrams, likelihoodEntries;

	public LanguageDetector() {
		try {
			load();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LanguageDetector(int numberClasses) {
		this.n_classes = numberClasses;
		try {
			load();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() throws IOException,
			ClassNotFoundException {
		String resourcePath = findResourceDirPath();
		likelihood = SimpleMatrix.loadBinary(resourcePath + "\\trigram.count.per.class");

		n_classes = likelihood.numRows();
		MatrixOperations mo = new MatrixOperations();
		prior = mo.initialiseMatrix(prior, n_classes, 1);
		prior = SimpleMatrix.loadBinary(resourcePath + "\\prior");

		likelihoodEntries = loadAllTrigramString();
	}

	private String findResourceDirPath() {
		return System.getProperty("user.dir")
				+ ".src.main.resources.".replaceAll("\\.", "\\\\")
				+ LanguageDetector.class
						.getCanonicalName()
						.substring(
								0,
								LanguageDetector.class.getCanonicalName()
										.lastIndexOf(".")).toString()
						.replaceAll("\\.", "\\\\");
	}

	private List<String> loadAllTrigramString() throws ClassNotFoundException,
			IOException {
		ObjectInputStream ois;
		ois = new ObjectInputStream(this.getClass().getResourceAsStream(
				"all.trigrams"));
		likelihoodEntries = (List<String>) ois.readObject();
		return likelihoodEntries;
	}

	public int predict(String sentence) {
		splitSentenceToTrigrams(sentence);
		List<Double> langDetectProbabilities = new ArrayList<Double>(n_classes);
		int tmpClass = 0;
		while (tmpClass < n_classes) {
			double individualProbability = 0.0;
			for (String trigram : trigrams) {
				double prob = (likelihoodEntries.contains(trigram)) ? smoothedindividualProbability(
						tmpClass, trigram) : 0.0001;
				individualProbability += Math
						.log(prob / prior.get(tmpClass, 0));
			}
			langDetectProbabilities.add(individualProbability);
			tmpClass++;
		}
		return argmax(langDetectProbabilities);
	}

	private void splitSentenceToTrigrams(String sentence) {
		trigrams = new ArrayList<String>();
		NgramOperations to = new NgramOperations();
		trigrams = to.readTextToNgrams(sentence, 3);
	}

	private double smoothedindividualProbability(int tmpClass, String trigram) {
		return (likelihood.get(tmpClass, likelihoodEntries.indexOf(trigram)) != 0.0) ? likelihood
				.get(tmpClass, likelihoodEntries.indexOf(trigram)) : 0.0001;
	}

	private int argmax(List<Double> posteriors) {
		int cl = 0;
		double max = posteriors.get(0);
		for (Double posterior : posteriors) {
			if (posterior.doubleValue() > max) {
				cl = posteriors.indexOf(posterior);
				max = posteriors.get(cl);
			}
		}
		return cl;
	}
}