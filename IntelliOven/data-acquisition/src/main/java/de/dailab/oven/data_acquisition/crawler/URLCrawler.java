package de.dailab.oven.data_acquisition.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * @author Tristan Schroer
 * @param search String with recipe name to search for
 * @param urls List of all URLs found on the different pages (mainPages list)
 * Additionally uses a textfile to store known pages for recipes
 * Calls the crawler for each page (as long as we don't have generic one, we'll stay to call them seperately)
 * Supports adding and deleting pages from the list quite RESTful
 * File operations might be implemented in a new, more flexible class later on
 */

public class URLCrawler {
	//Input string for a given recipe name
	private final String search;
	private final String language;
	private static final Logger LOGGER = Logger.getLogger(URLCrawler.class.getName());

	//List of found URLs
	private List<String> urls = new ArrayList<>();
	
	//List of main URLs of recipe pages (https://chefkoch.de for instance), public to be able to add or remove a page
	//THIS LIST IS STORED IN A SIMPLE TEXTFILE
	private List<String> mainPages = new ArrayList<>();
	
	
	
	public URLCrawler(final String search, final String language) {
		this.search = search;
		this.language = language;
	}
	
	/*
	 * Get all URLs through calling the parser for each URL in the list of main pages
	 * @return List of URLs
	 * @throws IOException
	 */
	public List<String> getUrls() throws IOException {
		if(this.urls.isEmpty()) {
			this.urls = updateUrls();
		}
		return this.urls;
	}
	
	/*
	 * Update the URLs list if necessary
	 * @return List of URLs
	 * @throws IOException
	 */
	private List<String> updateUrls() throws IOException {
		final List<String> urlList = new ArrayList<>();
		if(this.mainPages.isEmpty()) {
			this.mainPages = getMainPages();
		}
		for(final String page : this.mainPages) {
			//use main pages by ending (de / com) - right dumb for now
			final GenericCrawler gc = new GenericCrawler(this.search, page, this.language);
			urlList.addAll(gc.getLinks());
		}
		
		return urlList;
	}
		
	/*
	 *  Adds page to mainPages and to the stored file
	 * @param page URL 
	 * @throws IOException
	 */
	public void addPage(final String page) throws IOException {
		//0. Check if the page is already existing
		boolean pageExists = false;
		for(final String mpage : this.mainPages) {
			if(page.equals(mpage)) {
				pageExists = true;
			}
		}
		
		if(!pageExists) {
			//1. Add page to the list of mainPages
			this.mainPages.add(page);
			
			//2. Add page to the File
			overwriteFile();
			
			//3. reload the links
			this.urls = updateUrls();
		}
		
	}
	

	
	/*
	 * Delete one page from mainPages (list and file)
	 * @param page URL
	 * @throws IOException
	 */
	public void deletePage(final String page) throws IOException {
		
		//Delete from list
		for(final String mpage : this.mainPages) {
			if(mpage.equals(page)) {
				this.mainPages.remove(mpage);
			}
		}
		
		//Delete from file by overwriting it 
		overwriteFile();
		
		//reload the links
		this.urls = updateUrls();
	}
	
	/**
	 *  Overwrite file safely (quite RESTful)
	 */
	private void overwriteFile() {
		final String mainPagesFileName = "mainPages.txt";
		final String mainPagesOldFileName = "mainPages_old.txt";
		final String mainPagesNewFileName = "mainPages_new.txt";
		
		//Rename the file, so that the stored data won't get lost
		renameFile(mainPagesFileName, mainPagesOldFileName);

		
		//Discard the current file, since the renaming creates a copy
		try {
			Files.deleteIfExists(Paths.get(mainPagesFileName));
		} catch (IOException e1) {
			LOGGER.log(Level.INFO, e1.getLocalizedMessage(), e1.getCause());
		}

		
		//Try to create the new file
		final File file = new File(mainPagesNewFileName);
		boolean success;
		try {
			success = file.createNewFile();
			if(!success)
				LOGGER.log(Level.INFO, "Could not create mainpages.txt");
		} catch (IOException e1) {
			LOGGER.log(Level.INFO, e1.getLocalizedMessage(), e1.getCause());
		}

		
		//Add each page to the new file
		for(final String page : this.mainPages) {
			addToFile(page, mainPagesNewFileName);
		}
		
		//Rename the new file to the right destination
		renameFile(mainPagesNewFileName, mainPagesFileName);
		
		//Discard files which are not longer used
		try {
			Files.deleteIfExists(Paths.get(mainPagesNewFileName));
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}
		try {
			Files.deleteIfExists(Paths.get(mainPagesOldFileName));
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}

	}
	
	/**
	 *  Add page to the storage file
	 * @param page URL
	 * @param fileName filename
	 */
	private void addToFile(final String page, final String fileName) {

        try (final FileWriter file = new FileWriter(fileName, true)) {
        	
        	// FileReader reads text files in the default encoding.
        	try(final FileReader fileReader = new FileReader(fileName)){
        		
        		try(final BufferedReader bufferedReader = new BufferedReader(fileReader)){
		            // If there is no page listed yet don't add an '\n' before writing the page to the file
        			final String line;
        			line = bufferedReader.readLine();
        			if(line == null) {
		            	file.write(page);
		            }
		            else {
		            	file.write("\n" + page);
		            }

        		} // close bufferedReader
        		
        	} // close fileReader
        	
        } catch (final IOException e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
        }
        
	}
	
	/*
	 * reads websites to crawl from (from file integration/recipe-analyzer/mainPages.txt)
	 * @return List of URLs
	 * @throws IOException
	 */
	private List<String> getMainPages() throws IOException{
		final String mainPagesFileName = "mainPages.txt";
		final String mainPagesOldFileName = "mainPages_old.txt";
		final String mainPagesNewFileName = "mainPages_new.txt";
		List<String> pageList = new ArrayList<>();
		//If the file doesn't exist it means, that either the program is executed for the first time or crashed before
		if(Paths.get(mainPagesFileName).toFile().exists()) {
			
			// FileReader reads text files in the default encoding
			try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(mainPagesFileName))){	
		            String line = null;
		            //Add lines to the list of main pages
		            while((line = bufferedReader.readLine()) != null) {
		            	pageList.add(line);
		            }
			}
	        catch (final Exception e) {
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
	        } //fileReader

		} // close if
		else {
			boolean success;
			//restore old data, if the last operation stopped working
			if(Paths.get(mainPagesOldFileName).toFile().exists()) {
				renameFile(mainPagesOldFileName, mainPagesFileName);
				
				//Call the function again, so that the old data is now loaded into the mainPages list
				pageList = getMainPages();
				
				//Check if the program crashed during creating the new version, if so, delete it
				Files.deleteIfExists(Paths.get(mainPagesNewFileName));
				
				//Discard the old one
				Files.deleteIfExists(Paths.get(mainPagesOldFileName));
			}
			//if no file exists at all, create a new one
			else {
				final File file = new File(mainPagesFileName);
				success = file.createNewFile();
				if(!success){
					LOGGER.log(Level.INFO,"mainPages.txt was created");
				}
			}	
		}
		return pageList;
	}

	public void renameFile(final String oldpath, final String newpath) {
		final File oldFile = new File(oldpath);
		final File newFile = new File(newpath);
		final boolean success = oldFile.renameTo(newFile);
		
		if(!success){
			LOGGER.log(Level.INFO,"Renaming from {0} to {1} was not successful",
					new Object[] {oldpath, newpath});
		}
	}
}
