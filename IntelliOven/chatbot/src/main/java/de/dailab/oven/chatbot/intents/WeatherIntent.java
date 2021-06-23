package de.dailab.oven.chatbot.intents;

import de.dailab.chatbot.aal.weather.WeatherConditions;
import de.dailab.chatbot.aal.weather.WeatherReporter;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;
import zone.bot.vici.intent.NamedEntity;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WeatherIntent extends IntelliOvenIntent {

	private static final Logger LOG = LoggerFactory.getLogger(WeatherIntent.class);

	public WeatherIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.GOODBYE, DialogState.WELCOME, DialogState.PROVIDE_RATING);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final Optional<NamedEntity> dayEntOpt = request.getNamedEntities().getSingle("day");
		final Optional<NamedEntity> typeEntOpt = request.getNamedEntities().getSingle("type");
		if(!dayEntOpt.isPresent()) {
			return IntentResponse.NOT_HANDLED;
		}
		final String day = dayEntOpt.get().getValue();
		final Map<String, Object> datamodel = new HashMap<>();
		datamodel.put("day", day);
		try {
			final WeatherConditions weatherConditions;
			if("TOMORROW".equals(day)) {
				weatherConditions = WeatherReporter.getConditionsTomorrow();
			} else if("DAY_AFTER_TOMORROW".equals(day)) {
				weatherConditions = WeatherReporter.getConditionsDayAfterTomorrow();
			} else {
				weatherConditions = WeatherReporter.getConditionsToday();
			}
			datamodel.put("weather", weatherConditions);
			if(typeEntOpt.isPresent() && "RAIN".equals(typeEntOpt.get().getValue())) {
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Weather.GetPrecipitation", datamodel);
			} else {
				getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Weather.GetTemperatures", datamodel);
			}
		} catch(final IOException e) {
			LOG.error("Could not retrieve weather information", e);
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "DefaultAnswers.INTERNAL_ERROR");
		}
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}
}
