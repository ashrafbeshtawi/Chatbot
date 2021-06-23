package de.dailab.oven.recommender;

import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Recipe;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Recommender {

    private Recommender() {
        //SOnarLint
    }

    public static List<Recipe> recommend(int servings, long userID) throws InterruptedException, ExecutionException, InputException, DatabaseException, ConfigurationException {

        String queryString = "MATCH (User)-[rating:RATED]->(Recipe)\n" +
                "WHERE ID(User) = " + userID + "\n" +
                "AND\n" +
                "rating.rating IS NOT NULL\n" +
                "WITH \n" +
                "//calculate the similarity \n" +
                "algo.similarity.euclideanDistance([rating.rating, Recipe.originalServings], [10," + servings + "]) AS similarity,\n" +
                "Recipe as ratedRecipe \n" +
                "ORDER BY similarity ASC \n" +
                "LIMIT 3 \n" +
                "\n" +
                "MATCH (food:Foodlabel)<-[:IS]-(ratedRecipe)-[:BELONGS_TO]->(cat:Category), \n" +
                "(ratedRecipe)-[:CONTAINS]->(Ingredient) \n" +
                "WITH ratedRecipe, food, cat,  \n" +
                "collect(id(Ingredient)) AS RatedIngredientVector \n" +
                "MATCH (ratedRecipe)<-[:AUTHORED]-(Author) \n" +
                "WITH ratedRecipe, food, cat, RatedIngredientVector,  \n" +
                "RatedIngredientVector + collect(id(Author))  AS ratedCharacteristicVector \n" +
                "\n" +
                "MATCH (cat)<-[:BELONGS_TO]-(recipe:Recipe)-[:IS]->(food)\n" +
                "OPTIONAL MATCH (User)-[:PREFERS]->(cat)\n" +
                "WHERE recipe <> ratedRecipe AND \n" +
                "size(ratedRecipe.instructions)-2 <=  size(recipe.instructions) AND size(recipe.instructions) <= size(ratedRecipe.instructions)+2\n" +
                "AND\n" +
                "ID(User) = " + userID + "\n" +
                "WITH recipe, ratedRecipe, ratedCharacteristicVector\n" +
                "MATCH (Author)-[:AUTHORED]->(recipe) \n" +
                "WITH recipe, ratedRecipe, ratedCharacteristicVector, \n" +
                "collect(id(Author)) AS AuthorVector \n" +
                "MATCH (recipe)-[:CONTAINS]->(Ingredient) \n" +
                "WITH recipe, ratedRecipe, ratedCharacteristicVector, AuthorVector, \n" +
                "AuthorVector + collect(id(Ingredient)) AS characteristicVector \n" +
                "RETURN recipe \n" +
                "ORDER BY algo.similarity.jaccard(characteristicVector, ratedCharacteristicVector)  DESC \n" +
                "LIMIT 8";

        Query query = new Query();
        return query.executeRecipeQueryString(queryString);

    }
}