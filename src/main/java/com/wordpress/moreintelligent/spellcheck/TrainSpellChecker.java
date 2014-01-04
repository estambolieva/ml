package com.wordpress.moreintelligent.spellcheck;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ekaterina Stambolieva <estambolieva@gmail.com>
 * 
 */
public class TrainSpellChecker implements WordFinder<HashSet<String>>{

	/**
	 * @param a
	 *            sequence of characters
	 * @return a no-duplicates set of the recognised words
	 */
	@Override
	public HashSet<String> findWords(String text) {
		HashSet<String> words = new HashSet<String>();
		Matcher m = wordPatt.matcher(text.toLowerCase());
		String word;
		while (m.find()) {
			word = m.group();
			if (!words.contains(word))
				words.add(word);
		}
		return words;
	}

	/**
	 * @param a
	 *            list of seen in training data words, duplicates allowed
	 */
	private HashMap<String, Double> train(ArrayList<String> features) {
		double count = 0.0;
		HashMap<String, Double> wordFrequencies = new HashMap<String, Double>();
		for (String w : features) {
			count = (Double) (wordFrequencies.containsKey(w) ? wordFrequencies
					.get(w) : 0.0);
			wordFrequencies.put(w, count + 1.0);
			count = 0.0;
			if (wordFrequencies.size() % 1000 == 0)
				System.out.println(wordFrequencies.size() + "...");
		}
		System.out.println(wordFrequencies.get("the"));
		System.out.println(wordFrequencies.get("this"));
		System.out.println(wordFrequencies.get("there"));
		System.out.println(wordFrequencies.get("therefore"));
		return wordFrequencies;
	}

	/**
	 * @param path
	 *            to training data file
	 * @throws IOException
	 */
	public void run(String pathToFile, String tempPath) throws IOException {
		ArrayList<String> features = new ArrayList<String>();

		LineNumberReader lReader = new LineNumberReader(new FileReader(
				new File(pathToFile)));
		String line;
		while ((line = lReader.readLine()) != null) {
			features.addAll(findWords(line));
		}
		HashMap<String, Double> featureFreqs = train(features);
		FeatureIO fio = new FeatureIO();
		fio.saveFeatures(featureFreqs, tempPath);
		System.out.println("Final size of training data. " + features.size());
		System.out.println("Final size of unique features. "
				+ featureFreqs.size());
//		createSpellingVariants(featureFreqs);
	}

//	private void createSpellingVariants(HashMap<String, Double> featureFreqs) {
//		HashSet<String> edits1 = new HashSet<String>(), edits2 = new HashSet<String>();
//		for (String word : featureFreqs.keySet()) {
//			HashSet<String> edits1PerWord = edits1(word.toLowerCase());
//			edits1.addAll(edits1PerWord);
//			edits2.addAll(edits2(edits1PerWord));
//		}
//	}

	/**
	 * main method
	 * 
	 * @param args
	 *            (path to training data file)
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TrainSpellChecker tsc = new TrainSpellChecker();
		try {
			tsc.run(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
