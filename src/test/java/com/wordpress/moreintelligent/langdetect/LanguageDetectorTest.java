package com.wordpress.moreintelligent.langdetect;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LanguageDetectorTest {
	
	LanguageDetector ld;

	@Before
	public void setUp() throws Exception {
		ld = new LanguageDetector();
	}

	@Test
	public void testPredictDE() {
		String germanSnippet = "Bei der diesjährigen Schachweltmeisterschaft wird der Titelverteidiger Viswanathan Anand von Magnus Carlsen (Bild) herausgefordert.";
		int label;
		label = ld.predict(germanSnippet);
		compareResults(label, 1);
	}
	
	@Test
	public void testPredictEN() {
		String englishSnippet = " Players use their avatars to explore the abandoned city of an ancient race known as the D'ni, uncover story clues and solve puzzles.";
		int label;
		label = ld.predict(englishSnippet);
		compareResults(label, 0);
	}
	
	@Test
	public void testPredictDA() {
		String danishSnippet = "Rødlos (Lynx rufus) er et nordamerikask rovdyr i kattefamilien. Der findes tolv underarter, som er udbredt fra det sydlige Canada over det meste af USA til det nordlige Mexico.";
		int label;
		label = ld.predict(danishSnippet);
		compareResults(label, 2);
	}
	
	@Test
	public void testPredictIT() {
		String danishSnippet = "L'Alto Mantovano è una zona geografica posta a nord-ovest della città di Mantova nell'omonima provincia e al confine con le province di Brescia e di Verona. È delimitata a nord dalle colline moreniche del lago di Garda, a est dalla provincia di Verona, a nord-ovest da quella di Brescia, e a sud dalla pianura del Medio Mantovano.";
		int label;
		label = ld.predict(danishSnippet);
		compareResults(label, 3);
	}
	
	private void compareResults(int actualLabel, int expectedClass) {
		assertTrue(actualLabel == expectedClass);
	}

}