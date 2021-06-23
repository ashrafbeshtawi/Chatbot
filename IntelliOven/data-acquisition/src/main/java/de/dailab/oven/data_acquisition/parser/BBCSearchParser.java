package de.dailab.oven.data_acquisition.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/*
 * 
 * @author Tristan Schroer
 * @input HTMLDoc The result page of a search, sent by a crawler
 * @output List of URLs (just those on the first result page)
 * 
 * IMPORTANT: No exception handling implemented yet (dealing with null/undefined stuff)
 * 
 * Just a small class for now, could be a basement for a generic, more complex one
 */

public class BBCSearchParser {

	//Input name of recipe to look up
	private final Document htmlToParse;

	public BBCSearchParser(final Document htmlToParse){
		this.htmlToParse = htmlToParse;
	}
	
	/*
	 * Get the links to all of the results on the first page (30 in case of chefkoch)
	 * @return
	 * @throws IOException
	 */
	public List<String> getLinks() throws IOException{
		//Sadly they used div classes instead of articles
		final Elements articles = this.htmlToParse.select("#main-content").select(".food-body").select(".food-grid").select(".food-content-wrapper").select(".gel-wrap").select(".gel-layout").select(".gel-layout__item");
		final List<String> urls = new ArrayList<>();
		for(final Element article : articles) {
			if(article.select("a").attr("abs:href").contains("recipes")) {
				urls.add((article.select("a").attr("abs:href")));
			}
		}
		return urls;
	}
}
