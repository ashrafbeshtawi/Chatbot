package de.dailab.oven.model.data_module.test;

import de.dailab.oven.model.data_model.*;
import org.junit.Assert;
import org.junit.Test;

import zone.bot.vici.Language;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.*;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import org.junit.Before;

public class RecipeTest {
	
	Recipe recipe;
	Logger logger;
	
	@Before
	public void initialize() {
		this.recipe = new Recipe();
		this.logger = Logger.getGlobal();
	}
	
	@Test
	public void authorTest() {
		assertEquals("unknown", recipe.getAuthor());
		
		recipe.setAuthor(null);
		assertEquals("unknown", recipe.getAuthor());
		
		recipe.setAuthor("");
		assertEquals("unknown", recipe.getAuthor());
		
		recipe.setAuthor("Max Mustermann");
		assertEquals("max mustermann", recipe.getAuthor());
		
		recipe.setAuthor("Max Mustermann");
		assertEquals("max mustermann", recipe.getAuthor());
	}
	
	@Test
	public void languageTest() {
		assertEquals(Language.UNDEF, recipe.getLanguage());
		
		Language nullLanguage = null;
		recipe.setLanguage(nullLanguage);
		assertEquals(Language.UNDEF, recipe.getLanguage());
		
		recipe.setLanguage(Language.GERMAN);
		assertEquals(Language.GERMAN, recipe.getLanguage());
	}
	
	@Test
	public void nameTest() {
		assertEquals("", recipe.getName());
		
		recipe.setName(null);
		assertEquals("", recipe.getName());
		
		recipe.setName("");
		assertEquals("", recipe.getName());
		
		recipe.setName("Lecker Möhrchen");
		assertEquals("lecker möhrchen", recipe.getName());
		
		recipe.setName("");
		assertEquals("", recipe.getName());
	}
	
	@Test
	public void categoriesTest() {
		
		Set<Category> testCategories = new HashSet<>();
		testCategories.add(new Category("cat1"));
		testCategories.add(new Category("cat2"));
		testCategories.add(new Category("cat3"));
		
		Set<String> emptySet = new HashSet<>();
		
		
		assertEquals(emptySet, recipe.getCategories());
		
		recipe.setCategories(null);
		assertEquals(emptySet, recipe.getCategories());
		
		recipe.setCategories(testCategories);
		assertEquals(testCategories, recipe.getCategories());
		
		recipe.addCategory(null);
		assertEquals(testCategories, recipe.getCategories());
		
		recipe.addCategory(new Category("cat3"));
		assertEquals(testCategories, recipe.getCategories());
		
		testCategories.add(new Category("cat4"));
		recipe.addCategory(new Category("cat4"));
		assertEquals(testCategories, recipe.getCategories());
		
		recipe.removeCategory(null);
		assertEquals(testCategories, recipe.getCategories());
		
		recipe.removeCategory(new Category("cat5"));
		assertEquals(testCategories, recipe.getCategories());
		
		testCategories.remove(new Category("cat4"));
		recipe.removeCategory(new Category("cat4"));
		assertEquals(testCategories, recipe.getCategories());
	}
	
	@Test
	public void durationTest() {
		Duration zeroDuration	= Duration.ZERO;
		Duration nullDuration	= null;
		Duration duration30		= Duration.ZERO.plusMinutes(30);
		
		//Initialization
		assertEquals(zeroDuration, recipe.totalDuration());
		
		//Set null
		recipe.setDuration(nullDuration);
		assertEquals(zeroDuration, recipe.totalDuration());
		
		//Set equal duration
		recipe.setDuration(zeroDuration);
		assertEquals(zeroDuration, recipe.totalDuration());
		
		//Set new duration
		recipe.setDuration(duration30);
		assertEquals(duration30, recipe.totalDuration());
	}
	
	public void durationsTest() {
		Duration zeroDuration	= Duration.ZERO;
		Duration nullDuration	= null;
		Duration negativeDuration = Duration.ZERO.minusHours(1);
		Duration duration30		= Duration.ZERO.plusMinutes(30);
		Duration duration1		= Duration.ZERO.plusHours(1);
		Duration duration130	= Duration.ZERO.plusHours(1).plusMinutes(30);
		
		Map<String, Duration> emptyMap = new HashMap<>();
		Map<String, Duration> testMap  = new HashMap<>();
		Map<String, Duration> nullMap  = null;
		
		testMap.put("preparation time", duration30);
		
		
		//Initialization
		assertEquals(emptyMap, recipe.getDurations());
		
		//Set null
		recipe.setDurations(nullMap);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Set equal durations
		recipe.setDurations(emptyMap);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Set new durations
		recipe.setDurations(testMap);
		assertEquals(testMap, recipe.getDurations());
		
		//Add null to map
		recipe.addDurationToListOfDurations(null, 1, 0);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add null to map (second method)
		recipe.addDurationToListOfDurations(null, duration1);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add null to map (second method)
		recipe.addDurationToListOfDurations("preparation", nullDuration);
		assertEquals(emptyMap, recipe.getDurations());
										
		//Add negative duration to list to map
		recipe.addDurationToListOfDurations("preparation", -1, -30);
		assertEquals(emptyMap, recipe.getDurations());
								
		//Add negative duration to list to map
		recipe.addDurationToListOfDurations("preparation", -1, 0);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add negative duration to list to map
		recipe.addDurationToListOfDurations("preparation", 0, -30);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add negative duration to list to map
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", -1, 30);
		assertEquals(testMap, recipe.getDurations());
		
		//Add negative duration to list to map
		testMap.remove("preparation");
		testMap.put("preparation", duration1);
		recipe.addDurationToListOfDurations("preparation", 1, -30);
		assertEquals(testMap, recipe.getDurations());
		
		//Add negative duration (second method)
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", negativeDuration);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add zero duration
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", 0, 0);
		assertEquals(emptyMap, recipe.getDurations());
		
		//Add zero duration (second method)
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", zeroDuration);
		assertEquals(emptyMap, recipe.getDurations());

		//Add valid duration
		testMap.clear();
		testMap.put("preparation", duration30);
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", 0, 30);
		assertEquals(testMap, recipe.getDurations());
		
		//Add valid duration (second method)
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", duration30);
		assertEquals(testMap, recipe.getDurations());
		
		//Remove null
		recipe.removeDurationFromListOfDurations(null);
		assertEquals(testMap, recipe.getDurations());
		
		//Remove valid
		recipe.removeDurationFromListOfDurations("preparation");
		assertEquals(emptyMap, recipe.getDurations());
		
		//Ensure getDuration works right
		recipe.addDurationToListOfDurations("preparation", duration30);
		recipe.addDurationToListOfDurations("baking", duration1);
		assertEquals(duration130, recipe.totalDuration());
		
		recipe.setDurations(emptyMap);
		recipe.addDurationToListOfDurations("preparation", duration30);
		recipe.addDurationToListOfDurations("baking", duration1);
		recipe.addDurationToListOfDurations("total", duration130);
		assertEquals(duration130, recipe.totalDuration());
	}
	
	@Test
	public void foodLabelTest() {
		
		recipe.setFoodLabel(null);
		assertEquals(FoodLabel.UNDEF, recipe.getFoodLabel());
		
		recipe.setFoodLabel(FoodLabel.GREEN);
		assertEquals(FoodLabel.GREEN, recipe.getFoodLabel());
	}
	
	@Test
	public void idTest() {
		assertEquals(-1, recipe.getId());
		long id = 12345;
		recipe.setId(id);
		assertEquals(id, recipe.getId());
	}
	
	@Test
	public void renderedImageTest() {
		assertEquals(null, recipe.getImage());
		
		recipe.setImage(null);
		assertEquals(null, recipe.getImage());

		BufferedImage image = null;
		File imageFile = null;

	    try{
	    	String filePath = new File("").getAbsolutePath();
	    	imageFile = new File(filePath.concat("/src/test/TestImage.png"));
	    	image = ImageIO.read(imageFile);
	    }catch(IOException e){
	    	logger.warning(e.toString() + "\n" + imageFile);
	    }
	    
		recipe.setImage(image);
		assertEquals(image, recipe.getImage());
	}
	
	@Test
	public void imageFileTest() {
		assertEquals(null, recipe.getImage());
		
		recipe.setImageFile(null);
		assertEquals(null, recipe.getImage());
		
		File imageFile = null;
		try {
	    	String filePath = new File("").getAbsolutePath();
	    	imageFile = new File(filePath.concat("/src/test/TestImage.png"));
		}catch(Exception e){
	    	logger.warning(e.toString() + "\n" + imageFile);
		}
		
		recipe.setImageFile(imageFile);
		assertEquals(imageFile, recipe.getImageFile());
	}
	
	@Test
	public void imagePathtest() {
		//New initialization since previous images / image files should not be in there
		recipe = new Recipe();
		assertEquals("", recipe.getImagePath());
		
		recipe.setImagePath(null);
		assertEquals("", recipe.getImagePath());
		
		recipe.setImagePath("");
		assertEquals("", recipe.getImagePath());

		recipe.setImagePath("Kein Pfad");
		assertEquals("Kein Pfad", recipe.getImagePath());
		
		String imagePath = new File("").getAbsolutePath().concat("/src/test/resources/TestImage.png");
		recipe.setImagePath(imagePath);
		assertEquals(imagePath, recipe.getImagePath());
		
//		File imageFile = null;
//		try {
//	    	imageFile = new File(imagePath);
//		}catch(Exception e){
//	    	logger.warning(e.toString() + "\n" + imageFile);
//		}
//		
//		assertEquals(imageFile, recipe.getImageFile());
//		assertTrue(recipe.isImageFileFromDatabase());
//		
//		RenderedImage image = null;
//	    try{
//	    	image = ImageIO.read(imageFile);
//	    }catch(IOException e){
//	    	logger.warning(e.toString() + "\n" + imageFile);
//	    }
//	    assertEquals(image.getSampleModel(), recipe.getImage().getSampleModel());
//	    assertTrue(recipe.isImageFromDatabase());
	}
	
	//@Test
	public void ingredientsTest() {
		List<IngredientWithAmount> emptyMap = new LinkedList<>();
		List<IngredientWithAmount> testMap = new LinkedList<>();
		
		assertEqualIngLists(emptyMap, recipe.getIngredients());
		
		recipe.setIngredients(null);
		assertEqualIngLists(emptyMap, recipe.getIngredients());
		
		recipe.setIngredients(emptyMap);
		assertEqualIngLists(emptyMap, recipe.getIngredients());
		
		Ingredient ingredient = new Ingredient("Bertie Botts Bohnen", Language.GERMAN);
		
		recipe.setLanguage(Language.GERMAN);
		
		recipe.addIngredientToListOfIngredients(null);
		assertEqualIngLists(emptyMap, recipe.getIngredients());
		
		recipe.addIngredientToListOfIngredients(new IngredientWithAmount(ingredient, 2, Unit.PIECES));
		IngredientWithAmount testIngredient = recipe.getIngredients().get(0);
		assertTrue(testIngredient.getIngredient().getLanguage() == Language.GERMAN);
		testMap.add(testIngredient);
		assertEqualIngLists(testMap, recipe.getIngredients());
		
		recipe.addIngredientToListOfIngredients(testIngredient);
		assertEqualIngLists(testMap, recipe.getIngredients());
		
		recipe.removeIngredient(null);
		assertEqualIngLists(testMap, recipe.getIngredients());
		
		recipe.removeIngredient(testIngredient.getIngredient());
		assertEqualIngLists(emptyMap, recipe.getIngredients());
	}

	private void assertEqualIngLists(@Nonnull final List<IngredientWithAmount> expected, @Nonnull final List<IngredientWithAmount> actual) {
		Assert.assertEquals(expected.size(), actual.size());
		for(int i=0; i<expected.size(); i++) {
			Assert.assertTrue(Math.abs(expected.get(i).getQuantity() - actual.get(i).getQuantity()) < 0.001f);
			Assert.assertEquals(expected.get(i).getUnit(), actual.get(i).getUnit());
		}
	}
	
	@Test
	public void instructionsTest() {
		List<String> testList = new ArrayList<>();
		assertEquals(testList, recipe.getInstructions());
		
		recipe.setInstructions(null);
		assertEquals(testList, recipe.getInstructions());
		
		recipe.addInstruction(null);
		assertEquals(testList, recipe.getInstructions());
		
		recipe.addInstruction("short");
		assertEquals(testList, recipe.getInstructions());
		
		testList.add("Long enough");
		recipe.setInstructions(testList);
		assertEquals(testList, recipe.getInstructions());
		
		recipe.addInstruction("to work a lot");
		testList.add("to work a lot");
		assertEquals(testList, recipe.getInstructions());
	}
	
	@Test
	public void originalServingsTest() {
		assertEquals(0, recipe.getOriginalServings());
		
		recipe.setOriginalServings(0);
		assertEquals(0, recipe.getOriginalServings());
		
		recipe.setOriginalServings(42);
		assertEquals(42, recipe.getOriginalServings());
		
		recipe.setOriginalServings(-1);
		assertEquals(42, recipe.getOriginalServings());
	}
	
	@Test
	public void urlTest() {
		assertEquals("", recipe.getUrl());
		
		recipe.setUrl(null);
		assertEquals("", recipe.getUrl());
		
		recipe.setUrl("");
		assertEquals("", recipe.getUrl());
		
		recipe.setUrl("http://www.dai-labor.de/");
		assertEquals("http://www.dai-labor.de/", recipe.getUrl());
	}
	
	@Test
	public void userRatingTest() {
		Map<Long, Integer> testRatings = new HashMap<>();
		testRatings.put((long) 1, 2);
		testRatings.put((long) 2, 4);
		testRatings.put((long) 3, 6);
		
		Map<Long, Integer> emptyMap = new HashMap<>();
		
		assertEquals(emptyMap, recipe.getUserRatings());
		
		recipe.setUserRatings(null);
		assertEquals(emptyMap, recipe.getUserRatings());
		
		recipe.setUserRatings(testRatings);
		assertEquals(testRatings, recipe.getUserRatings());
		
		recipe.addUserRating(1, 3);
		testRatings.replace((long) 1, 3);
		assertEquals(testRatings, recipe.getUserRatings());
		assertEquals(3, recipe.getUserRatings().get((long) 1).intValue());
		
		recipe.addUserRating(4, -2);
		testRatings.put((long) 4, -2);
		assertEquals(testRatings, recipe.getUserRatings());
		assertTrue(recipe.getUserRatings().containsKey((long) 4));
		
		recipe.removeUserRating(0);
		assertEquals(testRatings, recipe.getUserRatings());
		
		testRatings.remove((long) 4);
		recipe.removeUserRating(4);
		assertEquals(testRatings, recipe.getUserRatings());
		assertFalse(recipe.getUserRatings().containsKey((long) 4));
	}
}
