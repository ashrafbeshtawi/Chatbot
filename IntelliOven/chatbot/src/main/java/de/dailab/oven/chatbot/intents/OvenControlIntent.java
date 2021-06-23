package de.dailab.oven.chatbot.intents;

import de.dailab.brain4x.nlp.utils.turkish.TurkishWordsToNumber;
import de.dailab.oven.DummyOven;
import de.dailab.oven.OvenProgram;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OvenControlIntent extends IntelliOvenIntent {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(OvenControlIntent.class);

	@Nonnull
	private final DummyOven oven;

	public OvenControlIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, @Nonnull final DummyOven oven) {
		super(channel, appState, DialogState.GOODBYE, DialogState.WELCOME, DialogState.PROVIDE_RATING);
		this.oven = oven;
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final NamedEntities entities = request.getNamedEntities();
		final Optional<NamedEntity> actionCodeEntity = entities.getSingle("action");
		if(!actionCodeEntity.isPresent()) {
			LOG.info("Action code missing");
			return IntentResponse.NOT_HANDLED;
		}
		return handle(request, request.getMessage().getLanguage(), actionCodeEntity.get().getValue(), entities);
	}

	@Nonnull
	private IntentResponse handle(@Nonnull final IntentRequest request, @Nonnull final Language language, @Nonnull final String action, @Nonnull final NamedEntities entities) {
		switch(action) {
			case "SetProgram":
				return handleSetProgram(language, entities, false);
			case "PreheatOven":
				return handleSetProgram(language, entities, true);
			case "TurnOvenOff":
				this.oven.switchOff();
				getOutputChannel().sendMessageToUser(language, "OvenControl.TurnOvenOff");
				return IntentResponse.HANDLED;
			case "SwitchOvenLight":
				return handleSwitchOvenLight(language, entities);
			default:
				LOG.warn("Unknown action code");
				return IntentResponse.NOT_HANDLED;
		}
	}

	@Nonnull
	private IntentResponse handleSwitchOvenLight(@Nonnull final Language language, @Nonnull final NamedEntities entities) {
		final Optional<NamedEntity> stateEntity = entities.getSingle("state");
		if(!stateEntity.isPresent()) {
			LOG.warn("Target state of oven light not specified");
			return IntentResponse.NOT_HANDLED;
		}
		if("ON".equals(stateEntity.get().getValue())) {
			this.oven.switchLampOn();
			getOutputChannel().sendMessageToUser(language, "OvenControl.TurnOvenLightOn");
			return IntentResponse.HANDLED;
		} else if("OFF".equals(stateEntity.get().getValue())) {
			this.oven.switchLampOff();
			getOutputChannel().sendMessageToUser(language, "OvenControl.TurnOvenLightOff");
			return IntentResponse.HANDLED;
		}
		LOG.warn("Unknown action code");
		return IntentResponse.NOT_HANDLED;

	}

	@Nonnull
	private IntentResponse handleSetProgram(@Nonnull final Language language, @Nonnull final NamedEntities namedEntities, final boolean preheat) {
		if(this.oven.getPrograms().isEmpty()) {
			// TODO send message
			return IntentResponse.HANDLED;
		}
		final OvenProgram program = getProgram(this.oven.getPrograms(), namedEntities.getSingle("ovenProgram"));
		final Integer targetTemperature = getSetpointTemperature(language, namedEntities.getSingle("tempSetpoint"), program);
		try {
			this.oven.setProgram(program, targetTemperature, null, language.getLangCode2());
			final Map<String, Object> datamodel = new HashMap<>();
			datamodel.put("programName", program.getName());
			datamodel.put("degrees", targetTemperature);
			getOutputChannel().sendMessageToUser(language, "OvenControl.SetProgram", datamodel);
			// TODO send message when preheated
			return IntentResponse.HANDLED;
		} catch(final IllegalArgumentException e){
			getOutputChannel().sendMessageToUser(language, "OvenControl.UnsupportedTemperature");
			return IntentResponse.HANDLED;
		}
	}

	@Nonnull
	private static OvenProgram getProgram(@Nonnull @NotEmpty final List<OvenProgram> programs, @Nonnull final Optional<NamedEntity> programEntityOpt) {
		if(programEntityOpt.isPresent()) {
			for(final OvenProgram p : programs) {
				if(programEntityOpt.get().getValue().equals(p.getName())) {
					return p;
				}
			}
		}
		return OvenProgram.STATIC;
	}

	@Nullable
	private static Integer getSetpointTemperature(@Nonnull final Language language, @Nonnull final Optional<NamedEntity> temperatureEntity, @Nonnull final OvenProgram program) {
		if(!temperatureEntity.isPresent()) {
			return program.getDefaultTemperature();
		}
		try {
			return Integer.parseInt(new TurkishWordsToNumber().apply(temperatureEntity.get().getValue(), language));
		} catch(final NumberFormatException e) {
			return program.getDefaultTemperature();
		}
	}

}
