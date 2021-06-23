package de.dailab.oven.model.data_module.test;

import org.junit.Test;
import static org.junit.Assert.*;

import de.dailab.oven.model.data_model.Unit;
import zone.bot.vici.Language;

public class UnitTest {
	
	@Test
	public void unitTest() {
		final Unit unit = Unit.UNDEF;
		assertEquals(Unit.TEASPOON, unit.getUnitFromAbbreviation("teelöffel"));
		assertEquals(Unit.TABLESPOON, unit.getUnitFromAbbreviation("yemek kaşığı"));
		assertEquals(Unit.FLUIDOUNCE, unit.getUnitFromAbbreviation("floz"));
		assertEquals(Unit.CUP, unit.getUnitFromAbbreviation("tasse"));
		assertEquals(Unit.PINT, unit.getUnitFromAbbreviation("p"));
		assertEquals(Unit.QUART, unit.getUnitFromAbbreviation("q"));
		assertEquals(Unit.GALON, unit.getUnitFromAbbreviation("galon"));
		assertEquals(Unit.LITERS, unit.getUnitFromAbbreviation("l"));
		assertEquals(Unit.MILLILITERS, unit.getUnitFromAbbreviation("milliliters"));
		assertEquals(Unit.DECILITER, unit.getUnitFromAbbreviation("decilitre"));
		assertEquals(Unit.GRAM, unit.getUnitFromAbbreviation("gr"));
		assertEquals(Unit.OUNCE, unit.getUnitFromAbbreviation("oz"));
		assertEquals(Unit.POUND, unit.getUnitFromAbbreviation("pound"));
		assertEquals(Unit.MILLIGRAM, unit.getUnitFromAbbreviation("mg"));
		assertEquals(Unit.KILOGRAM, unit.getUnitFromAbbreviation("kg"));
		assertEquals(Unit.INCH, unit.getUnitFromAbbreviation("in"));
		assertEquals(Unit.MILLIMETER, unit.getUnitFromAbbreviation("mm"));
		assertEquals(Unit.CENTIMETER, unit.getUnitFromAbbreviation("centimeter"));
		assertEquals(Unit.DECIMETER, unit.getUnitFromAbbreviation("dm"));
		assertEquals(Unit.METER, unit.getUnitFromAbbreviation("m"));
		assertEquals(Unit.PIECES, unit.getUnitFromAbbreviation("stk"));
		assertEquals(Unit.KCAL, unit.getUnitFromAbbreviation("kilocalorie"));
		assertEquals(Unit.UNDEF, unit.getUnitFromAbbreviation("keineEinheit"));
	}
}
