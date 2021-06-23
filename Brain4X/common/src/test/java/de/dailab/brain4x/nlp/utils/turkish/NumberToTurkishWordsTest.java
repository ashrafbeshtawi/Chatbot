package de.dailab.brain4x.nlp.utils.turkish;

import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.Language;

public class NumberToTurkishWordsTest {

	private final NumberToTurkishWords converter = new NumberToTurkishWords();

	@Test
	public void test1() {
		Assert.assertEquals("sıfır", converter.apply("0", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test2() {
		Assert.assertEquals("a sıfır b", converter.apply("a 0 b", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test3() {
		Assert.assertEquals("yuz", converter.apply("100", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test4() {
		Assert.assertEquals("iki yuz beş0 uç bin dokuz yuz bir0 yedi", converter.apply("253917", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test5() {
		Assert.assertEquals("sıfır virgul yuz iki0 uç bin dört yuz beş0 altı", converter.apply("00.123456", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test6() {
		Assert.assertEquals("iki0 uç virgul dokuz0 sekiz", converter.apply("23.9800", Language.TURKISH).toLowerCase());
	}

	@Test
	public void test7() {
		Assert.assertEquals("bir0 bir virgul sıfır sıfır altı0 yedi", converter.apply("11.0067", Language.TURKISH).toLowerCase());
	}

}
