package de.dailab.oven.model.data_model;

/**
 * Enum of nutrition. The default unit is mostly gram
 */

public enum Nutrition {
    WATER("Water", Unit.GRAM),
    ENERGY("Energy", Unit.KCAL),                               			// Energiegehalt, meistens in kcal
    PROTEIN("Protein", Unit.GRAM),                             			// Eiweiss
    CARBOHYDRATE("Carbohydrate, by difference", Unit.GRAM),    			// Kohlenhydrate insgesamt
    FIBER("Fiber, total dietary", Unit.GRAM),                  			// Ballaststoffe
    SUGAR("Sugars, total", Unit.GRAM),                         			// Zucker von den Kohlehydraten
    FAT("Total lipid (fat)", Unit.GRAM),                       			// Gesamtfettanteil
    SATFAT("Fatty acids, total saturated", Unit.GRAM);       			// Gesättigte Fettsäuren vom Gesamtfettanteil

    // Salz nicht vorhanden? Nur einzelne Minerale aber auch kein Cl ??

    private String usdaDarstellung;
    private Unit standartUnit;

    Nutrition(String s, Unit unit) {
        usdaDarstellung = s;
        standartUnit = unit;
    } 

    public Unit getStandartUnit(){
        return this.standartUnit;
    }

    @Override
    public String toString(){
        return usdaDarstellung;
    }
    
    public String toVariable() {
    	return this.name().toLowerCase();
    }
}
