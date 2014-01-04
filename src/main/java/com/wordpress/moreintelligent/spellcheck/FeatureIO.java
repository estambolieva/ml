package com.wordpress.moreintelligent.spellcheck;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class FeatureIO {

	public void saveFeatures(HashMap<String, Double> obj, String tempPath) throws IOException {
		File file = new File(tempPath);
		FileOutputStream f = new FileOutputStream(file);
		ObjectOutputStream s = new ObjectOutputStream(f);
		s.writeObject(obj);
		s.flush();
		s.close();
		System.out.println("Saved feature object to file.");
	}

	/**
	 * reads binary file with a map of words and theirs corresponding
	 * frequencies in the data
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, Double> loadFeatures(String name)
			throws IOException, ClassNotFoundException {
		HashMap<String, Double> loadedFeatureFreqs = new HashMap<String, Double>();
		InputStream is = TrainSpellChecker.class
				.getResourceAsStream("features");
		ObjectInputStream s = new ObjectInputStream(is);
		loadedFeatureFreqs = (HashMap<String, Double>) s.readObject();
		s.close();
		return loadedFeatureFreqs;
	}
}
