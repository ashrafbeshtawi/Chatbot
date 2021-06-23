package de.dailab.oven.recipe_services.nutrition_evaluator;

import de.dailab.oven.model.data_model.*;
import de.dailab.oven.recipe_services.unit_recalculator.UnitRecalculator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutritionsOnCall{



    private final URIBuilder uriB;
    private final CloseableHttpClient httpclient;


    /**
     * Erstellt ein neues NutritionsOnCall Objekt und stellt die Standartparameter für eine REST
     * Anfrage an die Nährwertdatenbank.
     */
    public NutritionsOnCall(){
        this.uriB = new URIBuilder()
                .setScheme("https")
                .setHost("api.nal.usda.gov")
                .setParameter("api_key", "FJ4rufodEwsLcIAEIJEChRm4PX6vXfXmeZSwcRBU")
                .setParameter("max", "5");
        this.httpclient = HttpClients.createDefault();
    }

    /**
     * Sums up all the nutritions of all the ingredients of this recipe. Takes into consideration,
     * that the nutritions are relative to 100 gram of ingredient and therefor needs some recalculation.
     * The sums are given in gram and are recalculated for 100 gram of the recipe as reference.
     * @param recipe The recipe, which gets its nutritions summed up.
     * @return {@code EnumMap<Nutrition,Amount>} as the sum of nutritions in this recipe
     */
    private EnumMap<Nutrition,Amount> sumNutOverRecipe(final de.dailab.oven.model.data_model.Recipe recipe){
        final EnumMap<Nutrition, Amount> nutSum = new EnumMap<>(Nutrition.class);
		final UnitRecalculator ur = new UnitRecalculator();
		ur.recalculate(recipe);
        final List<IngredientWithAmount> ingredients = recipe.getIngredients();
        float recipeWeight = 0;
        for(final IngredientWithAmount ing : ingredients){
            recipeWeight += ing.getQuantity();
        }
        if(recipeWeight==0) recipeWeight = 100;
        final float recipeRelTo100 = 100 / recipeWeight;
        for(final Nutrition n : Nutrition.values()){
            float newAmount = 0;
            for(final IngredientWithAmount ing : ingredients){
                final Map<Nutrition, Amount> nuts = ing.getIngredient().getNutrition();
                final float relTo100 = ing.getQuantity() / 100;
                try {
                	newAmount += nuts.get(n).getQuantity() * relTo100;
                } catch(final Exception e) {
                	newAmount += 0;
                }
            }
            Unit unit = Unit.GRAM;
            if(Nutrition.ENERGY == n) unit = Unit.KCAL;
            nutSum.put(n,new Amount(newAmount * recipeRelTo100, unit));
        }
        return nutSum;
    }

    /**
     * Returns an evaluation in FoodLabel form of the given recipe.
     * @param recipe The recipe, which gets its nutrition evaluated.
     * @return FoodLabel, an Enum with three states to determine the value of the recipe.
     */
    public FoodLabel evaluateNutritions(final de.dailab.oven.model.data_model.Recipe recipe){
        final EnumMap<Nutrition,Amount> nutSum = sumNutOverRecipe(recipe);
        FoodLabel foodLabel = FoodLabel.GREEN;
        //Kriterien der Reihe nach Abfragen, ob eines Anschlägt und die FoodLabel färbt!
        if(nutSum.get(Nutrition.SUGAR).getQuantity() > 10 ||
                nutSum.get(Nutrition.PROTEIN).getQuantity() > 20 ||
                nutSum.get(Nutrition.ENERGY).getQuantity() > 1500 ||
                nutSum.get(Nutrition.SATFAT).getQuantity() > 6 ||
                nutSum.get(Nutrition.FAT).getQuantity() > 25 ||
                nutSum.get(Nutrition.CARBOHYDRATE).getQuantity() > 50) foodLabel = FoodLabel.YELLOW;
        if(nutSum.get(Nutrition.SUGAR).getQuantity() > 15 ||
                nutSum.get(Nutrition.PROTEIN).getQuantity() > 25 ||
                nutSum.get(Nutrition.ENERGY).getQuantity() > 2000 ||
                nutSum.get(Nutrition.SATFAT).getQuantity() > 10 ||
                nutSum.get(Nutrition.FAT).getQuantity() > 40 ||
                nutSum.get(Nutrition.CARBOHYDRATE).getQuantity() > 70) foodLabel = FoodLabel.RED;
        return foodLabel;
    }

    /**
     * Gibt eine Liste an Nährwertlisten vom Typ {@code List<EnumMap<Nutrition,Amount>>} zurück. Erwartet dafür
     * eine Liste an ingredients, wie sie direkt im Recipe vorkommt. Also {@code List<Map.Entry<Ingredient, Amount>}.
     * @param ingredients eine liste an ingredients wie sie direkt im Recipe stehen!
     * @return eine Liste an EnumMaps vom Typ Nutrition,Amount, in der Reihenfolge der eingegebenen ingredients.
     */
    @SuppressWarnings("unused")
    public List<Map<Nutrition,Amount>> getNutritionsEntrysMultiple(final List<Map.Entry<Ingredient, Amount>> ingredients){
        final LinkedList<Map<Nutrition,Amount>> resultList = new LinkedList<>();

        for(final Map.Entry<Ingredient,Amount> ing : ingredients){
            final Map<Nutrition, Amount> nuts = getNutritionsEntrys(ing.getKey().getName());
            resultList.add(nuts);
        }

        return resultList;
    }

    /**
     * Gibt eine Liste an Nährwerteinträgen zu einem Produktnamen zurück für 100g Produktmenge.
     * Benutzt die Methode getNutritions().
     * @param productName Produkt, dessen Nährwerte gesucht werden.
     * @return Eine {@code EnumMap<Nutrition, Amount>} mit dem Nährwertnamen und der Menge
     *
     */
    public Map<Nutrition, Amount> getNutritionsEntrys(final String productName){
        final JSONArray nuts = this.getNutritions(productName);
        final EnumMap<Nutrition, Amount> resultList = new EnumMap<>(Nutrition.class);
        if(nuts == null) return resultList;
        for(int i = 0; i < nuts.length(); ++i){
            final JSONObject nextNut = nuts.getJSONObject(i);
            final String nextNutName =  nextNut.getString("name");
            for(final Nutrition n : Nutrition.values()){
                if(n.toString().equals(nextNutName)){
                    final String nutUnit = nextNut.getString("unit");
                    final Amount mount = new Amount(Float.valueOf(nextNut.getString("value")),nutUnit);
                    resultList.put(n, mount);
                }
            }
        }
        for(final Nutrition n : Nutrition.values()){
            resultList.computeIfAbsent(n, v -> new Amount(0, Unit.GRAM));
        }
        return resultList;
    }

    /**
     * Gibt ein Documentobjekt aus Jsoup zurück, welches die Nährwerte zu dem Produkt enthält.
     * @param productName Produkt, dessen Nährwerte gesucht werden.
     * @return Documentobjekt aus Jsoup
     */
    private JSONArray getNutritions(final String productName){
        final String ndbno = this.getNDBNo(productName);
        if(ndbno.equals("")) return null;

        final Document doc = makeCallForDoc(false, ndbno);
        JSONArray json = null;
        if(doc != null) json = new JSONObject(doc.body().text()).getJSONObject("report").getJSONObject("food").getJSONArray("nutrients");

        return json;
    }

    /**
     * Sucht zu einem Produktnamen die passende NDBNo (Datenbanknummer), mit dessen Hilfe dann
     * eine eindeutige Anfrage nach einem JSON Dokument mit Nährwertangaben durchgeführt werden
     * kann.
     * Bisher werden die ersten 5 Ergebnisse aus der USDA Datenbank abgefragt und lediglich die ndbno des
     * ersten Eintrages zurückgegeben!
     * @param productName Produkt, zu dem eine NDBNo gesucht wird.
     * @return NDBNo Leerer String, wenn kein Datenbankeintrag existiert.
     */
    private String getNDBNo(final String productName){
        String ndbno = "";

        final Document doc = makeCallForDoc(true, productName);
        if(doc != null){
            final Element first = doc.body();
            final JSONObject json = new JSONObject(first.text());
            try{
                ndbno = json.getJSONObject("list").getJSONArray("item").getJSONObject(0).getString("ndbno");
            }
            catch(final JSONException e){
                // Wenn kein Datenbankeintrag existiert
                Logger.getLogger(NutritionsOnCall.class.toString()).log(Level.WARNING, e.getMessage());
            }
        }

        return ndbno;
    }


    /**
     * This method makes the REST call to the USDA. It makes either a call to the "Searches" API or
     * the "Food Reports" API, based on the Param callForNDBNO.
     * @param callForNDBNO if true, this method calls the "Searches" API, else the "Food Reports" API.
     * @param callParam The param which gets used in the REST call. Either a product name for the "Searches"
     *                  API or a ndb no for the "Food Reports" API.
     * @return The result of the REST call.
     */
    private Document makeCallForDoc(final boolean callForNDBNO, final String callParam){
        Document doc = null;
        URI uri = null;
        try {
            if(callForNDBNO){
                this.uriB.setPath("/ndb/search/")
                        .setParameter("q",callParam);
            }
            else {
                this.uriB.setPath("/ndb/reports/")
                        .setParameter("ndbno",callParam);
            }
            uri = this.uriB.build();
        } catch (final URISyntaxException e) {
            Logger.getLogger(NutritionsOnCall.class.toString()).log(Level.WARNING, e.getMessage());
        }
        final HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = null;
        try {
            response = this.httpclient.execute(httpget);
        } catch (final IOException e) {
            Logger.getLogger(NutritionsOnCall.class.toString()).log(Level.WARNING, e.getMessage());
        }
        try {
            HttpEntity entity = null;
            if (response != null) {
                entity = response.getEntity();
            }
            if (entity != null) {
                final InputStream istream = entity.getContent();
                doc = Jsoup.parse(istream,"UTF-8","");
                }
        } catch(final IOException | JSONException e){ // hier wird abgefangen, dass eine Zutat nicht in der USDA enthalten ist.
        } finally{
            try {
                if (response != null) {
                    response.close();
                }
            } catch (final IOException e) {
                Logger.getLogger(NutritionsOnCall.class.toString()).log(Level.WARNING, e.getMessage());
            }
        }
        return doc;
    }

}
