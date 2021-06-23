package de.dailab.oven.model.data_module.test;

import org.junit.Test;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Unit;

import static org.junit.Assert.*;

public class AmountTest {
	
	@Test
	public void amountTest() {
		Amount testAmount = new Amount();
		final Unit nullUnit = null;
		final String nullString = null;
		assertTrue((float) 0 == testAmount.getQuantity());
		assertTrue(Unit.UNDEF == testAmount.getUnit());
		testAmount.setQuantity((float) 0.2);
		assertTrue((float) 0.2 == testAmount.getQuantity());
		testAmount.setUnit(nullUnit);
		assertTrue(Unit.UNDEF == testAmount.getUnit());
		testAmount.setUnit(nullString);
		assertTrue(Unit.UNDEF == testAmount.getUnit());
		
		
		testAmount = new Amount(5, Unit.PIECES);
		assertTrue((float) 5 == testAmount.getQuantity());
		assertTrue(Unit.PIECES == testAmount.getUnit());
		
		testAmount = new Amount((float) 0.5, Unit.DECILITER);
		assertTrue((float) 0.5 == testAmount.getQuantity());
		assertTrue(Unit.DECILITER == testAmount.getUnit());

		
		testAmount = new Amount((float) 1.337, "mm");
		assertTrue((float) 1.337 == testAmount.getQuantity());
		assertTrue(Unit.MILLIMETER == testAmount.getUnit());
		assertEquals("1.337 MILLIMETER", testAmount.toString());

	}
	
}
