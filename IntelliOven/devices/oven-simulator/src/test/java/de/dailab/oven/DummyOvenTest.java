package de.dailab.oven;

import org.junit.Assert;
import org.junit.Test;

public class DummyOvenTest {

    @Test(timeout = 10000)
    public void test() throws InterruptedException {
        final DummyOven oven = new DummyOven(200);
        oven.setProgram(OvenProgram.BOTTOM_HEATING, 200, null, null);
        Assert.assertEquals(OvenProgram.BOTTOM_HEATING, oven.getProgram());
        Assert.assertEquals(Integer.valueOf(200), oven.getTargetTemperature());
        float lastTemperature = oven.getCurrentTemperature();
        Thread.sleep(1000);
        Assert.assertTrue(oven.getCurrentTemperature()>lastTemperature);
        Thread.sleep(1000);
        Assert.assertTrue(oven.getCurrentTemperature()>100);
        Thread.sleep(1500);
        Assert.assertTrue(oven.getCurrentTemperature()>190);
        oven.setProgram(OvenProgram.STATIC, 150, null, null);
        Thread.sleep(1000);
        Assert.assertTrue(oven.getCurrentTemperature()>160);
        Assert.assertTrue(oven.getCurrentTemperature()<190);
        oven.switchOff();
        Thread.sleep(1000);
        Assert.assertTrue(oven.getCurrentTemperature()<140);
    }

    @Test
    public void testOvenLamp() {
        final DummyOven oven = new DummyOven(200);
        oven.switchLampOn();
        Assert.assertTrue(oven.isLampOn());
        oven.switchLampOff();
        Assert.assertFalse(oven.isLampOn());
    }

}
