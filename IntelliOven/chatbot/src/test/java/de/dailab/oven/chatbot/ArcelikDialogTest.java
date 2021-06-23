package de.dailab.oven.chatbot;

import de.dailab.oven.DummyOven;
import de.dailab.oven.OvenProgram;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import zone.bot.vici.Language;
import zone.bot.vici.test.ChatbotTest;
import zone.bot.vici.test.ChatbotTestMatcher;
import zone.bot.vici.test.matcher.ResponseMessageMatcher;

import java.util.LinkedList;
import java.util.List;

public class ArcelikDialogTest {

	private static final User user1 = new User();
	private static final List<Recipe> recipes = new LinkedList<>();
	private final UserController userController = Mockito.mock(UserController.class);
	private final DatabaseController databaseController = Mockito.mock(DatabaseController.class);
	private final IntelliOvenAppState appState = new IntelliOvenAppState();
	private final DummyOven oven = new DummyOven();

	@BeforeClass
	public static void init() {
		user1.setId(1);
		user1.setName("User 1");
		user1.addLanguageToSpokenLanguages(Language.ENGLISH.getLangCode2());
		user1.addIncompatibleIngredient(new Ingredient("tomato", Language.ENGLISH));
		for(int i=0; i<20; i++) {
			final Recipe recipe = new Recipe();
			recipe.setId(i);
			recipe.setName("Recipe "+i);
			recipe.setAuthor("Dummy");
			for(int j=0; j<3; j++) {
				final Ingredient ingredient = new Ingredient("Ingredient "+j, Language.TURKISH);
				recipe.addIngredientToListOfIngredients(new IngredientWithAmount(ingredient, j*100f, Unit.GRAM));
				recipe.addInstruction("Description step "+j+".");
			}
			recipes.add(recipe);
		}
	}

	private ChatbotTest initChatbotTest() throws Exception {
		Mockito.when(this.userController.getUserById(-1)).thenReturn(null);
		Mockito.when(this.userController.getUserById(1)).thenReturn(new User());
		Mockito.when(this.userController.addAndGetUser(Mockito.any())).thenAnswer((Answer) invocation -> invocation.getArguments()[0]);
		Mockito.when(this.databaseController.getRecipes(Mockito.any())).thenReturn(recipes);
		return ChatbotTest.configure(new ArcelikSkillResolver(), new FallbackHandler())
				.registerApi(this.appState, IntelliOvenAppState.class)
				.registerApi(this.userController, UserController.class)
				.registerApi(this.databaseController, DatabaseController.class)
				.registerApi(this.oven, DummyOven.class).build();
	}


	@Test
	public void testNormal() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Smalltalk.HowOldAreYou")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Help")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kullanabileceğim komutlar neler");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 35 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeConfirmation.ConfirmRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "evet isterim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetPreparationTime")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "ne kadar sürer");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "yemeği hazırlamak için hangi malzemeler gerekli");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "adımlar neler");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "sonraki adıma geçelim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RateRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tamamladım");
		test.verify();
		Assert.assertEquals(DialogState.PROVIDE_RATING, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("ProvideRating")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tarifi 5 olarak oylamak istiyorum");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("OvenControl.SetProgram")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "fırını 200 dereceye ayarla");
		Assert.assertEquals(OvenProgram.STATIC.getName(), this.oven.getProgram().getName());
		Assert.assertEquals(Integer.valueOf(200), this.oven.getTargetTemperature());
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Goodbye")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "görüşürüz");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());
	}

	@Test
	public void testRejectTheRecipe() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Smalltalk.HowOldAreYou")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Help")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kullanabileceğim komutlar neler");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 50 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeConfirmation.DontConfirmRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "hayır istemem");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());
	}

	@Test
	public void testWithoutRecipeConfirmation() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Smalltalk.HowOldAreYou")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Help")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kullanabileceğim komutlar neler");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 35 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetPreparationTime")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "ne kadar sürer");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "yemeği hazırlamak için hangi malzemeler gerekli");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RateRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tamamladım");
		test.verify();
		Assert.assertEquals(DialogState.PROVIDE_RATING, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Goodbye")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "görüşürüz");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());
	}

	@Test
	public void testChangeTheRecipe() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Smalltalk.HowOldAreYou")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Help")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kullanabileceğim komutlar neler");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 50 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetRecipeAlternative")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "başka bir tarif önerir misin");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetPreparationTime")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "ne kadar sürer");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "yemeği hazırlamak için hangi malzemeler gerekli");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RateRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tamamladım");
		test.verify();
		Assert.assertEquals(DialogState.PROVIDE_RATING, this.appState.getDialogState());

	}

	@Test
	public void testRecipeConfirmationAndStep() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Smalltalk.HowOldAreYou")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Help")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kullanabileceğim komutlar neler");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 35 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER_CONFIRMATION")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER_CONFIRMATION")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "kaç yaşındasın");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "yemeği hazırlamak için hangi malzemeler gerekli");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.NO_RECIPE_SELECTED_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.NO_RECIPE_SELECTED_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece 35 dakikam var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RateRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "bitirdim");
		test.verify();
		Assert.assertEquals(DialogState.PROVIDE_RATING, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

	}

	@Test
	public void testStartWithGoodbye() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "görüşürüz");
		test.verify();
		Assert.assertEquals(DialogState.WELCOME, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "evet isterim");
		test.verify();
		Assert.assertEquals(DialogState.WELCOME, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "adımlar neler");
		test.verify();
		Assert.assertEquals(DialogState.WELCOME, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tarifi 5 olarak oylamak istiyorum");
		test.verify();
		Assert.assertEquals(DialogState.WELCOME, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.AddDietType")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "vejeteryanım");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());
	}

	@Test
	public void testOtherStuff() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("DefaultAnswers.USER_NOT_LOGGED_IN")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "nuts alerjim var");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserPreferences.FilterAllergies")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "nuts alerjim var");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("OvenControl.SetProgram")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "ızgara moduna getir");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("OvenControl.SetProgram")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "fırını iki yüz yirmi dereceye ayarla");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.GetWeeklyPlan")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "haftalık tarif planı önerir misin lütfen");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Cooking.RecipeSearchWithIngredients")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "tavuk içeren bir yemek istiyorum ama sadece bir saat var");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_CONFIRMATION, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeConfirmation.ConfirmRecipe")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "evet isterim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.GetRecipeName")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "bana tarifi açıkla");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "adımlar neler");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "şuanki adıma geçelim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "sonraki adıma geçelim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "sonraki adıma geçelim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "bir önceki adıma geçerelim misin");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "sonraki adıma geçelim");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("RecipeStep.Step")).build();
		chatbotTest.sendMessage(Language.TURKISH, 1, "son adımdan önceki adıma geçelim misin");
		test.verify();
		Assert.assertEquals(DialogState.RECIPE_STEP, this.appState.getDialogState());

	}

	@Test(timeout = 15000)
	public void testUserAuth() throws Exception {
		final ChatbotTest chatbotTest = initChatbotTest();

		ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("Welcome.Hi")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "selam");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserAuthentication.StartEnrollment")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "sesimi kaydetmek istiyorum");
		test.verify();
		Assert.assertEquals(DialogState.ENROLLING, this.appState.getDialogState());
		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withMessage("hey asista, merhaba, kaç yaşındasın")).build();
		Thread.sleep(8000);
		test.verify();
		Assert.assertEquals(DialogState.ENROLLING, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserAuthentication.CompleteEnrollment")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "voice training completed");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserAuthentication.StartEnrollment")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "sesimi kaydetmek istiyorum");
		test.verify();
		Assert.assertEquals(DialogState.ENROLLING, this.appState.getDialogState());

		test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.TURKISH).withTemplateKey("UserAuthentication.AbortEnrollment")).build();
		chatbotTest.sendMessage(Language.TURKISH, -1, "abort");
		test.verify();
		Assert.assertEquals(DialogState.GOODBYE, this.appState.getDialogState());

	}

}
