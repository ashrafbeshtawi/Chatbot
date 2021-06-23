package de.dailab.oven.data_acquisition.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import de.dailab.oven.data_acquisition.parser.ChefkochSearchParser;
import de.dailab.oven.data_acquisition.parser.SpoonfulSearchParser;
import zone.bot.vici.Language;

public class GenericCrawler {

	private final String search;
	private final String destinationAddress;
	private final String language;
	private static final Logger LOGGER = Logger.getLogger(GenericCrawler.class.getName());
	/**
	 * @param search search term
	 * @param destinationAddress URL of homepage of website to crawl from
	 * @param language
	 */
	public GenericCrawler(final String search, final String destinationAddress, final String language) {
		this.search = search;
		this.destinationAddress = destinationAddress;
		this.language = language;
	}
	

	//THIS MIGHT BE A THING FOR A COMPLETE GENERIC CRAWLER TO FIND ALWAYS THE RIGHT FORM TO USE
	private FormElement findSearchForm(final String link) throws IOException{
		final Document mainPage = Jsoup.connect(link).get();
		return (FormElement) mainPage.select("form").first();
	}
	

	private void fillForm(final FormElement searchForm) {
		try {
			final Elements inputField = searchForm.select("input");
			inputField.val(this.search);			
		} catch (Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}
	}
	
	/*
	 * sends search form to main page
	 * @return URL of result page
	 * @throws IOException
	 */
	private URL url() {
		try {
			//Find the search form on the page
			final FormElement searchForm = findSearchForm(this.destinationAddress);
			//Fill the form with the given search string
			fillForm(searchForm);
			//Send the form back to main page (e.g. chefkoch)
			final Connection.Response searchRe = searchForm.submit().execute();
			//Return the redirected URL
			return searchRe.url();
		}
		catch(final Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			return null;
		}
	}
	
	/*
	 * Download the HTML-Doc of the result page to the given recipe name
	 * @return Document containing the results
	 * @throws IOException
	 */
	private Document getResultHtml() {
		try {
			//Jsoup.connect needs a string type
			final URL url = this.url();
			if(url != null){
				return Jsoup.connect(url.toString()).get();
			}
			else{
				return null;
			}
		}
		catch(final Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			return null;
		}
	}

	/*
	 * parses links from html with results of search
	 * @return list of URLs with recipes
	 * @throws IOException
	 */
	public List<String> getLinks() {		
		final List<String> linksList = new ArrayList<>();
		if(Language.getLanguage(this.language).equals(Language.GERMAN) ) {
			if(this.destinationAddress.contains("chefkoch")) {
				final ChefkochSearchParser csp = new ChefkochSearchParser(getResultHtml());
				linksList.addAll(csp.getLinks()); 
			}
			// call other german SearchParser here
			
		}
		else if(Language.getLanguage(this.language).equals(Language.ENGLISH)
				&& this.destinationAddress.contains("spoonful")) {
			final SpoonfulSearchParser sfsp = new SpoonfulSearchParser(getResultHtml());
			linksList.addAll(sfsp.getLinks());
		
			// call other german SearchParser here
		}
		//implement options more languages here
		
		return linksList;
	}
	
}
