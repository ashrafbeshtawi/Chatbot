package de.dailab.oven.chatbot.intents;

import de.dailab.oven.DummyOven;
import de.dailab.oven.OvenProgram;
import de.dailab.oven.model.IntelliOvenAppState;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import zone.bot.vici.Language;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OvenControlIntentTest {

    @Nonnull
    private final IntelliOvenAppState appState = new IntelliOvenAppState();
    @Nonnull
    private final DummyOven oven = new DummyOven(200);
    @Nonnull
    private final MessageOutputChannel outputChannel = Mockito.mock(MessageOutputChannel.class);
    @Nonnull
    private final InputMessage inputMessage = new SimpleInputMessage(Language.UNDEF, "");

    private IntentRequest createRequest(@Nonnull final InputMessage message, @Nonnull final NamedEntities entities) {
        final IntentRequest request = Mockito.mock(IntentRequest.class);
        Mockito.when(request.getMessage()).thenReturn(message);
        Mockito.when(request.getNamedEntities()).thenReturn(entities);
        return request;
    }

    private NamedEntities createNamedEntities(@Nullable final String action, @Nullable final String state, @Nullable final String ovenProgram, @Nullable final String tempSetpoint) {
        final Map<String, List<NamedEntity>> map = new HashMap<>();
        addSingleEntityToMap(map, "action", action);
        addSingleEntityToMap(map, "state", state);
        addSingleEntityToMap(map, "ovenProgram", ovenProgram);
        addSingleEntityToMap(map, "tempSetpoint", tempSetpoint);
        return new NamedEntities(map);
    }

    private void addSingleEntityToMap(@Nonnull final Map<String, List<NamedEntity>> map, @Nonnull final String key, @Nullable final String value) {
        if(value != null) {
            map.put(key, Collections.singletonList(new StaticNamedEntity(key, value)));
        }
    }

    @Test
    public void testOvenLampControl() {
        oven.switchLampOff();
        final OvenControlIntent intent = new OvenControlIntent(outputChannel, appState, oven);
        final InputMessage inputMessage = new SimpleInputMessage(Language.UNDEF, "");
        final NamedEntities lampOnEntities = createNamedEntities("SwitchOvenLight", "ON", null, null);
        final NamedEntities lampOffEntities = createNamedEntities("SwitchOvenLight", "OFF", null, null);
        intent.handle(createRequest(inputMessage, lampOnEntities));
        Assert.assertTrue(oven.isLampOn());
        intent.handle(createRequest(inputMessage, lampOffEntities));
        Assert.assertFalse(oven.isLampOn());
    }

    @Test
    public void testSwitchOff() {
        OvenProgram program = oven.getPrograms().get(0);
        oven.setProgram(program, program.getDefaultTemperature(), null, null);
        Assert.assertTrue(oven.isOn());
        final OvenControlIntent intent = new OvenControlIntent(outputChannel, appState, oven);
        final NamedEntities switchOffEntities = createNamedEntities("TurnOvenOff", null, null, null);
        intent.handle(createRequest(inputMessage, switchOffEntities));
        Assert.assertFalse(oven.isOn());
    }

}
