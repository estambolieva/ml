package com.wordpress.moreintelligent.langdetect;

import java.util.ArrayList;
import java.util.List;

public class NgramOperations {

	public List<String> readTextToNgrams(String line, int len) {
		List<String> trigrams = new ArrayList<String>();
		String trigram;
		int index = 0;
		while (hasNextNgram(line, index, len)) {
			trigram = readNgram(line, index, len);
			trigrams.add(trigram);
			index++;
		}
		return trigrams;
	}

	public boolean hasNextNgram(String text, int index, int len) {
		return ((index + 3 <= text.length()) && (text
				.substring(index, index + len) != null));
	}

	public String readNgram(String line, int index, int len) {
		return line.substring(index, index + len);
	}

}
