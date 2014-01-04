package com.wordpress.moreintelligent.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class SpellChecker implements WordFinder<LinkedList<String>> {

	private HashMap<String, Double> features;
	private LinkedList<String> words = new LinkedList<String>();

	private SpellChecker() {
		try {
			FeatureIO fio = new FeatureIO();
			features = fio.loadFeatures("features");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String correct(String word) {
		HashSet<String> edits1word = edits1(word);
		HashMap<String, Double> wordMap = new HashMap<String, Double>();
		wordMap.put(word, (features.containsKey(word)) ? features.get(word)
				: 0.01);
		HashMap<String, Double> wordEdits1 = addCandidates(edits1word);
		HashMap<String, Double> wordEdits2 = addCandidates(edits2(edits1word));
		System.out.println(wordEdits1.size());
		System.out.println(wordEdits2.size());

		return (features.keySet().contains(word)) ? word
				: (features.keySet().contains((String) sortByValue(wordEdits1)) ? (String) sortByValue(wordEdits1)
						: (String) sortByValue(wordEdits2));
	}

	public HashSet<String> edits1(String word) {
		HashSet<List<String>> splits = splitWord(word);
		HashSet<String> edits1 = new HashSet<String>();

		HashSet<String> deletes = findDeletes(splits);
		HashSet<String> transponses = findTransponses(splits);
		HashSet<String> replaces = findReplaces(splits);
		HashSet<String> inserts = findInserts(splits);

		edits1.add(word);
		edits1.addAll(deletes);
		edits1.addAll(transponses);
		edits1.addAll(replaces);
		edits1.addAll(inserts);

		return edits1;
	}

	private HashSet<List<String>> splitWord(String word) {
		HashSet<List<String>> splits = new HashSet<List<String>>();
		int i = 0;
		while (i <= word.length()) {
			List<String> split = new ArrayList<String>();
			split.add(word.substring(0, i));
			split.add(word.substring(i, word.length()));
			splits.add(split);
			i++;
		}
		return splits;
	}

	private HashSet<String> findDeletes(HashSet<List<String>> splits) {
		HashSet<String> deleteSet = new HashSet<String>();
		for (List<String> parts : splits) {
			if (parts.get(1).length() > 1)
				deleteSet.add(parts.get(0) + parts.get(1).substring(1));
		}
		return deleteSet;
	}

	private HashSet<String> findTransponses(HashSet<List<String>> splits) {
		HashSet<String> transponseSet = new HashSet<String>();
		for (List<String> parts : splits) {
			if (parts.get(1).length() > 1)
				transponseSet.add(parts.get(0) + parts.get(1).substring(1, 2)
						+ parts.get(1).substring(0, 1)
						+ parts.get(1).substring(2));
		}
		return transponseSet;
	}

	private HashSet<String> findReplaces(HashSet<List<String>> splits) {
		HashSet<String> replaceSet = new HashSet<String>();
		for (List<String> parts : splits) {
			int ind = 0;
			while (ind < alphabet.length()) {
				if (parts.get(1).length() > 0)
					replaceSet.add(parts.get(0) + alphabet.charAt(ind)
							+ parts.get(1).substring(1));
				ind++;
			}
		}
		return replaceSet;
	}

	private HashSet<String> findInserts(HashSet<List<String>> splits) {
		HashSet<String> insertSet = new HashSet<String>();
		for (List<String> parts : splits) {
			int ind = 0;
			while (ind < alphabet.length()) {
				insertSet.add(parts.get(0) + alphabet.charAt(ind)
						+ parts.get(1));
				ind++;
			}
		}
		return insertSet;
	}

	/**
	 * Edits2.
	 *
	 * @param edits1Word the edits1 word
	 * @return the hash set
	 */
	public HashSet<String> edits2(HashSet<String> edits1Word) {
		HashSet<String> edits2 = new HashSet<String>();
		for (String entry : edits1Word)
			edits2.addAll(edits1(entry));
		return edits2;
	}


	/**
	 * Filters a set of probable spelling variants
	 *
	 */
	private HashSet<String> findKnownWords(HashSet<String> wordSet) {
		HashSet<String> known = new HashSet<String>();
		known.addAll(wordSet);
		known.retainAll((Collection<String>) features.keySet());
		return known;
	}

	/**
	 * Filters the HashSet of spelling variant candidates to a HashMap. Smoothes
	 * the newly seen candidates. Returns only the possible ones.
	 * 
	 * @param edits
	 *            a HashSet with probable spelling variants, most of which are
	 *            non existing real-world words
	 * @return a HashMap populated with spelling variant possible candidates
	 */
	private HashMap<String, Double> addCandidates(HashSet<String> edits) {
		HashMap<String, Double> cands = new HashMap<String, Double>();
		HashSet<String> knownWords = new HashSet<String>();
		knownWords = findKnownWords(edits);
		for (String cand1 : edits) {
			if (knownWords.contains(cand1))
				cands.put(cand1,
						(features.containsKey(cand1)) ? features.get(cand1)
								: 0.01);
		}
		return cands;
	}

	/**
	 * Sort a Map by value.
	 * 
	 * @param map
	 *            the map - expected a HashMap of candidate-value pairs
	 * @return the key of the map with the highest value, type - String
	 */
	private Object sortByValue(HashMap<String, Double> map) {
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return ((Double) ((Map.Entry<String, Double>) (o1)).getValue())
						.compareTo((Double) ((Map.Entry<String, Double>) (o2))
								.getValue());
			}
		});
		return list.get(list.size() - 1).getKey();
	}

	public void run(String sentence) {
		String correctedSentence = "";
		words.addAll(findWords(sentence));
		Set<String> setA = Sets.newHashSet(Splitter.fixedLength(1).split(
				punctuation));
		for (String word : words) {
			Set<String> setB = Sets.newHashSet(Splitter.fixedLength(1).split(
					word));
			setB.retainAll(setA);
			if (setB.size() == 0)
				correctedSentence += correct(word) + " ";
			else
				correctedSentence += word + " ";
		}
		System.out.println("Before: " + sentence);
		System.out.println("The result after spell checking: "
				+ correctedSentence);
	}

	public static void main(String[] args) throws IOException {
		SpellChecker sc = new SpellChecker();
		sc.run(args[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wordpress.moreintelligent.spellcheck.WordFinder#findWords(java.lang
	 * .String)
	 */
	@Override
	public LinkedList<String> findWords(String text) {
		LinkedList<String> words = new LinkedList<String>();
		Matcher m = wordPatt.matcher(text.toLowerCase());
		String word;
		while (m.find()) {
			word = m.group();
			if (!words.contains(word))
				words.add(word);
		}
		return words;
	}
}
