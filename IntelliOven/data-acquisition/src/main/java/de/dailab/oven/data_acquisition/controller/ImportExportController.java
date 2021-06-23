package de.dailab.oven.data_acquisition.controller;

import de.dailab.oven.data_acquisition.crawler.URLCrawler;
import de.dailab.oven.data_acquisition.parser.ArcelikParser;
import de.dailab.oven.data_acquisition.parser.ChefkochParser;
import de.dailab.oven.data_acquisition.parser.SpoonfulParser;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.recipe_analyzer.reactive.AsyncTask;
import de.dailab.oven.recipe_analyzer.reactive.IPromise;
import de.dailab.oven.recipe_services.nutrition_evaluator.NutritionsOnCall;
import zone.bot.vici.Language;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Tristan Schroer
 * 
 * For now there is just the use-case implemented, that we get a recipe name, for handling an url we'd need to check if it is one or not
 * Please implement your classes here, so that the analyzer can use them
 */

public class ImportExportController {

	private String search;
	private String language;
	private final List<de.dailab.oven.model.data_model.Recipe> recipes = new ArrayList<>();
	private List<String> urls = new ArrayList<>();

	private static final Logger LOGGER = Logger.getLogger(ImportExportController.class.getName());
	
	public ImportExportController(final String url){
		this.urls.add(url);
		if (url.contains(".com/"))
		{
			this.language = "en";
			this.search = "com";
		}
		else if (url.contains(".de/"))
		{
			this.language = "de";
			this.search = "de";
		}
		else
		{
			throw new IllegalArgumentException("The url must have a .com or .de domain");
		}
	}
	
	public ImportExportController(final String[] urls){
		for (final String url: urls){
			this.urls.add(url);
			
			if (url.contains(".com/")){
				this.language = "en";
				this.search = "com";
			} else if (url.contains(".de/")){
				this.language = "de";
				this.search = "de";
			}
		}
		
	}
	
	/**
	 * Empty for setting attributes later
	 */
	public ImportExportController() {}
	
	//Initially we need a recipe name to look for
	public ImportExportController(final String search, final String language) {
		this.search = search.toLowerCase();
		this.language = language; 
	}

	public List<de.dailab.oven.model.data_model.Recipe> getRecipes(){
		return this.recipes;
	}
	
	//Returns a list of recipes for a given recipe name
	public List<Recipe> getOnlineRecipesByUrls() throws IOException, InterruptedException{
		
		//throw exception if there haven't been any results
		if(this.urls.isEmpty()) {
			throw new IOException("Couldn't find any results.");
		}
		this.getParsedRecipes();
		this.generateFoodLabel();
		
		return this.recipes;
	}
	
	
	//Returns a list of recipes for a given recipe name
	public List<Recipe> getOnlineRecipesByName() throws Exception{
		this.getLinks();
		//throw exception if there haven't been any results
		if(this.urls.isEmpty()) {
			throw new IOException("Couldn't find any results.");
		}
		this.getParsedRecipes();
		this.generateFoodLabel();
		
		return this.recipes;
	}
	
	//Use the crawler to get links for a given recipe name
	private void getLinks() throws Exception {
		final URLCrawler uc = new URLCrawler(this.search, this.language);
		this.urls = uc.getUrls();
	}
	
	//Parse Recipes with correct parser and create a list of them
	private void getParsedRecipes() throws InterruptedException
	{
		final ConcurrentLinkedQueue<de.dailab.oven.model.data_model.Recipe> parsedRecipes = new ConcurrentLinkedQueue<>();
		final AtomicInteger threadCounter = new AtomicInteger(this.urls.size());
		final Object lockGuard = new Object();

		for(final String url: this.urls)
		{
			parseUrl(url)
				.subscribe(new IPromise<de.dailab.oven.model.data_model.Recipe, Exception>()
				{
					@Override
					public void onSuccess(final de.dailab.oven.model.data_model.Recipe result)
					{
						parsedRecipes.add(result);
						decrementCounterAndNotify();
					}

					@Override
					public void onError(final Exception reason)
					{
						decrementCounterAndNotify();
					}

					void decrementCounterAndNotify()
					{
						final int threadsLeft = threadCounter.decrementAndGet();
						if(threadsLeft == 0)
						{
							synchronized (lockGuard)
							{
								lockGuard.notifyAll();
							}
						}
					}

				});
		}

		// move all recipes from parsedRecipes into this.recipes when all threads are done
		synchronized (lockGuard)
		{
			while(threadCounter.get() > 0) {
				lockGuard.wait();				
			}
		}

		parsedRecipes.forEach(this.recipes::add);
	}

	private AsyncTask<de.dailab.oven.model.data_model.Recipe, Exception> parseUrl(final String url)
	{
		final AsyncTask<de.dailab.oven.model.data_model.Recipe, Exception> task = new AsyncTask<>();

		final Thread thread = new Thread(() ->
		{
			final SpoonfulParser sp = new SpoonfulParser();
			final ChefkochParser cp = new ChefkochParser();

			final de.dailab.oven.model.data_model.Recipe recipe = new de.dailab.oven.model.data_model.Recipe();
			recipe.setLanguage(Language.getLanguage(this.language));

			//BBC is not fully supported yet
			//Choose the right parser by URL (later on it should be chosen by the language)
			final String[] searchParts = this.search.split(" ");
			if(url.contains("spoonful") && url.contains(searchParts[0])) {
				try
				{
					sp.getRecipeFromUrl(url, recipe);
				}
				catch (final IOException e)
				{
					task.sendError(e);
					LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
				}
			}
			else if(url.contains("chefkoch")) {
				crawlChefkoch(url, task, cp, recipe);
			}

			task.sendSuccess(recipe);
		});
		thread.start();

		return task;
	}

	private void crawlChefkoch(final String url,
			final AsyncTask<de.dailab.oven.model.data_model.Recipe, Exception> task, final ChefkochParser cp,
			final de.dailab.oven.model.data_model.Recipe recipe) {
		
		for(int retryCounter = 0; retryCounter < 5; ++retryCounter)
		{
			try
			{
				cp.getRecipeFromUrl(url, recipe);
				break;
			}
			catch (final Exception e)
			{
				// Retry connection in 1 second
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
				try
				{
					Thread.sleep(100);
				}
				catch (final InterruptedException ex)
				{
					task.sendError(ex);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	private void generateFoodLabel() {
		final NutritionsOnCall noc = new NutritionsOnCall();
		for(final de.dailab.oven.model.data_model.Recipe recipe : this.recipes) {
			recipe.setFoodLabel(noc.evaluateNutritions(recipe));
		}
	}
	
	
	/**
	 * Tries to import arcelik recipes from the given file path into the default database
	 * @param dirPath The path of the directory where the recipes are located
	 * @return	NULL in case of lost database connection<br>
	 * 			True if importing succeeded<br>
	 * 			False otherwise (and if dirPath equals NULL)
	 * 			
	 */
	@Nullable
	public Boolean importArcelikRecipes(@Nullable final String dirPath) {
		
		if(dirPath != null) {
			try {
				final Query query = new Query();
				final File baseDir = new File(dirPath); 				
				query.putMultipleRecipes(ArcelikParser.parseRecipesInDirectory(baseDir));				
				return true;
				
			} catch (final Exception e) {
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
				return null;
			} 
				
		}
		
		return false;
	}
}
