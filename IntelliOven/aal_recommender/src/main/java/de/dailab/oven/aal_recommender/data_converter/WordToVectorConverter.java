package de.dailab.oven.aal_recommender.data_converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import zone.bot.vici.Language;

public class WordToVectorConverter {

	private final ArrayList<String> stopWords; // this words will be ignored
	private ArrayList<ArrayList<String>> wordLists; // all words that need conversion

	private ArrayList<String> dimensions; // consists oft the words from wordLists

	private static final Logger LOGGER = Logger.getLogger(WordToVectorConverter.class.getName());
	
	/**
	 * @param language  of the words
	 * @param wordLists all words that need conversion
	 */

	public WordToVectorConverter(final Language language, final ArrayList<ArrayList<String>> wordLists) {

		this.stopWords = new ArrayList<String>();
		loadStopWords(language);

		this.wordLists = wordLists;
		createDimensions();

	}

	private void createDimensions() {
		this.dimensions = new ArrayList<String>();
		final ArrayList<ArrayList<String>> newLists = new ArrayList<ArrayList<String>>();

		final Iterator<ArrayList<String>> it = this.wordLists.iterator();
		while (it.hasNext()) {
			final ArrayList<String> list = it.next();
			final ArrayList<String> newList = new ArrayList<String>();
			final Iterator<String> itList = list.iterator();

			while (itList.hasNext()) {
				final String str = itList.next();
				for (String word : str.split(" ")) {
					word = cleanWord(word);
					if (word != null) {
						newList.add(word);
						if (!this.dimensions.contains(word)) {
							this.dimensions.add(word);
						}
					}
				}
			}
			newLists.add(newList);

		}
		this.wordLists = newLists;

	}

	/**
	 * @param list of Strings to create a count vector for
	 * @return count vector of the input list
	 */

	public ArrayList<Double> getCountVec(final List<String> list) {
		final ArrayList<Double> vector = new ArrayList<Double>();
		for (int i = 0; i < this.dimensions.size(); i++) {
			vector.add(0.0);
		}

		final Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			final String str = it.next();
			for (String word : str.split(" ")) {
				word = cleanWord(word);
				final int index = this.dimensions.indexOf(word);

				if (index >= 0) {
					vector.set(index, vector.get(index) + 1);
				}
			}
		}

		return vector;

	}

	private void loadStopWords(final Language language) {
		File file = null;
		if (language.getLangCode2().equals("en") || language.getLangCode2().equals("EN")) {
			String path = WordToVectorConverter.class.getProtectionDomain().getCodeSource().getLocation().getPath();

			final String to_remove = "aal_recommender/target/classes/";
			final String resourcePath = "aal_recommender/src/main/resources/stopWords/enStopWords.txt";

			path = path.substring(0, path.length() - to_remove.length());
			path = path.concat(resourcePath);

			file = new File(path);
		}

		final Scanner input;
		try {
			input = new Scanner(file);

			while (input.hasNextLine()) {
				this.stopWords.add(input.nextLine());
			}
			input.close();
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}
	}

	private String cleanWord(final String word) {

		if (this.stopWords.contains(word)) {
			return null;
		}
		return word;
	}
}
