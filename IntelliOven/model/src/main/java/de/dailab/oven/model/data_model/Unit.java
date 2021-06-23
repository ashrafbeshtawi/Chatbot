package de.dailab.oven.model.data_model;

import de.dailab.oven.model.util.UnitTranslationsUtil;
import de.dailab.oven.model.util.UnitTranslationsUtil.UnitTranslationData;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Unit {
	
	/*Volume Units*/
	TEASPOON(new String[]{"teaspoon","t","tsp", "teaspoons", "çay kaşığı", "teelöffel", "tl", "teeloeffel"}),
	TABLESPOON(new String[]{"spoon", "spoons","tablespoon","tbl","tbs","tbsp", "tablespoons", "yemek kaşığı", "esslöffel","el","essloeffel"}),
	FLUIDOUNCE(new String[]{"fluidounce","floz","fl oz","fluid ounce"}),
	CUP(new String[]{"cup","c","cups","tasse"}),
	CAN(new String[]{"can","dose", "glas", "becher"}),
	HEAD(new String[] {"head", "kopf"}),
	PINT(new String[]{"pint","p","pt"}),
	QUART(new String[]{"quart","q","qt"}),
	GALON(new String[]{"galon","gal"}),
	LITERS(new String[]{"l", "Liter", "liter", "liters", "lt"}),
	MILLILITERS(new String[]{"ml","milliliter","millilitre","cc","milliliters"}),
	DECILITER(new String[]{"dl", "decilitre","deciliter","deziliter"}),
	
	/*Mass Units*/
	GRAM(new String[]{"g", "gram", "gramm","gramme", "gr"}),
	OUNCE(new String[]{"ounce","oz"}),
	POUND(new String[]{"pound","lb"}),
	MILLIGRAM(new String[]{"milligram","mg","milligramme"}),
	KILOGRAM(new String[]{"kilo","kg","kilogram","kilogramme"}),

	
	/*Length Units*/
	INCH(new String[]{"inch","in"}),
	MILLIMETER(new String[]{"mm","millimetre","millimeter"}),
	CENTIMETER(new String[]{"cm","centimetre","centimeter"}),
	DECIMETER(new String[]{"dm","decimetre","decimeter"}),
	METER(new String[]{"m","metre","meter"}),
	
	
	PIECES(new String[]{"pcs.", "pcs", "pieces","piece", "stk", "stk.","stück","stueck", "stick"}),
	KCAL(new String[]{"kcal","kilocalorie"}),
	UNDEF(new String[]{})
	;

	private final String[] abbreviations;

	private final Map<Language, UnitTranslationData> data;


	Unit(final String[] abbreviations) {
		this.abbreviations = abbreviations;
		final Map<Language, UnitTranslationData> map = new HashMap<>();
		for(final Language language : Language.getLanguages()) {
			map.put(language, UnitTranslationsUtil.getUnitLabels(language, name()));
		}
		this.data = Collections.unmodifiableMap(map);
	}

	@Nonnull
	public String getSingularLabel(@Nonnull final Language language) {
		return getLabels(language).getSingular();
	}

	@Nonnull
	public String getPluralLabel(@Nonnull final Language language) {
		return getLabels(language).getPlural();
	}

	@Nonnull
	public String getAbbreviation(@Nonnull final Language language){
		return getLabels(language).getAbbreviation();
	}

	public UnitTranslationData getLabels(@Nonnull final Language language){
		return this.data.get(language);
	}

	@Nonnull
	public String[] getAbbreviations(@Nonnull final Language language){
		return getLabels(language).getAbbreviations();
	}

	@Deprecated
	public String[] getUnitAbbrevations(){
		return this.abbreviations;
	}

	@Deprecated
	public Unit getUnitFromAbbreviation(String u){
		for (Unit utmp: Unit.values()){
			for(String stmp: utmp.abbreviations){
				if (stmp.equalsIgnoreCase(u)){
					return utmp;
				}
			}
		}
			
		return Unit.UNDEF;
	}

}
