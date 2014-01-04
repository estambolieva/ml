package com.wordpress.moreintelligent.spellcheck;

import java.util.regex.Pattern;

public interface WordFinder<T> {

	/**
	 * A sequence of allowed letter characters
	 * currently this is the English alphabet
	 */
	public final static String alphabet = "abcdefghijklmnopqrstuvwxyz";
	
	public final static String punctuation = "\"'.,;:?!/\\()[]{}-_+=|<>";
	
	/**
	 * The Word Patter recognises three types of series of characters as words
	 * i) a series of words 
	 * ii) a series of numbers 
	 * iii) a series of punctuation characters
	 */
	public final static String WORD_PATTERN = "(?:\\p{L}+|\\p{P}+|\\d+)";
	public static Pattern wordPatt = Pattern.compile(WORD_PATTERN);

	/**
	 * @param input text, a series of characters
	 * @return A data structure T which contains all identifies by the word
	 *         pattern words
	 */
	public T findWords(String text);
}
