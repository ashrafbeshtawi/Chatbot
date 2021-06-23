package de.dailab.oven.data_acquisition.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * 
 * @author Tristan Schroer
 * @input HTMLDoc: The result page of a search, sent by a crawler
 * @output List of URLs (just those on the first result page)
 * 
 * IMPORTANT: No exception handling implemented yet (dealing with null/undefined stuff)
 * 
 * Just a small class for now, could be a basement for a generic, more complex one
 */

public class ChefkochSearchParser {

	//Input name of recipe to look up
	private final Document htmlToParse;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChefkochSearchParser.class);

	
	public ChefkochSearchParser(final Document htmlToParse){
		this.htmlToParse = htmlToParse;
	}
	
	//Get the links to all of the results on the first page (30 in case of chefkoch)
	public List<String> getLinks() {
		final List<String> urls = new ArrayList<>();
		try {
			final Elements articles = this.htmlToParse.select("main").select("article");
			for(final Element article : articles) {
				urls.add((article.select("a").attr("abs:href")));
			}
		}
		catch(final Exception e){
			LOGGER.debug(e.getLocalizedMessage(), e.getCause());
		}
		
		return urls;
	}
}
