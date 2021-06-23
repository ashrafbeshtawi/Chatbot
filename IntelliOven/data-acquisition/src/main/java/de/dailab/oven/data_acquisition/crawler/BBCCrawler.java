package de.dailab.oven.data_acquisition.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import de.dailab.oven.data_acquisition.parser.BBCSearchParser;


public class BBCCrawler {

	//Input: name of recipe to look up
	private final String search;
	//Destination will be BBC in the beginning, it's just put in here as a var, so that it may be easier to adapt for a generic crawler 
	private final String destinationAddress;
	
	public BBCCrawler(final String search, final String destinationAddress) {
		this.search = search;
		this.destinationAddress = destinationAddress;
	}
	
	public List<String> getLinks() throws IOException {		
		final BBCSearchParser bbcsp = new BBCSearchParser(getResultHtml());
		final List<String> linksList = bbcsp.getLinks();
		return linksList;
	}

	private FormElement findSearchForm(final String link) throws IOException{
		final Document mainPage = Jsoup.connect(link).get();
		final FormElement searchForm = mainPage.select("form").forms().get(1);
		return searchForm;
	}

	private void fillForm(final FormElement searchForm) throws IOException{
		final Element inputField = searchForm.select("input").first();
		inputField.val(this.search);
	}

	private URL url() throws IOException{
		//Find the search form on the page
		final FormElement searchForm = findSearchForm(this.destinationAddress);
		//Fill the form with the given search string
		fillForm(searchForm);
		//Send the form back to BBC
		final Connection.Response searchRe = searchForm.submit().execute();
		//Return the redirected URL
		return searchRe.url();
	}

	private Document getResultHtml() throws IOException{
		//Jsoup.connect needs a string type
		final String urlString = this.url().toString();
		final Document doc = Jsoup.connect(urlString).get();
		return doc;
	}
}
