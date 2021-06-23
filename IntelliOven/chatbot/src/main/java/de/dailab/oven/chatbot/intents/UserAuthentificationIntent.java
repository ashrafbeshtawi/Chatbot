package de.dailab.oven.chatbot.intents;

import de.dailab.oven.database.UserController;
import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;
import zone.bot.vici.intent.NamedEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserAuthentificationIntent extends IntelliOvenIntent {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(UserAuthentificationIntent.class);

    @Nonnull
    private final UserController userController;

    @Nullable
    private User enrollingUser = null;

    public UserAuthentificationIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, @Nonnull final UserController userController) {
        super(channel, appState, IntelliOvenAppState.DialogState.GOODBYE, IntelliOvenAppState.DialogState.WELCOME, IntelliOvenAppState.DialogState.PROVIDE_RATING, IntelliOvenAppState.DialogState.USER_PREFERENCES, IntelliOvenAppState.DialogState.ENROLLING);
        this.userController = userController;
    }

    @Nonnull
    @Override
    public IntentResponse handle(@Nonnull final IntentRequest request) {
        final Optional<NamedEntity> actionCodeEntity = request.getNamedEntities().getSingle("action");
        if(!actionCodeEntity.isPresent()) {
            LOG.warn("Action code missing");
            return IntentResponse.NOT_HANDLED;
        }
        final Language language = request.getMessage().getLanguage();
        final String action = actionCodeEntity.get().getValue();
        if("StartEnrollment".equals(action)) {
            try {
                final User user = generateNewUser(this.userController.getAllUsers(), language);
                this.enrollingUser = this.userController.addAndGetUser(user);
                notifyAsrComponent();
            } catch (final Exception e) {
                LOG.error("Could not create a new user", e);
                getOutputChannel().sendMessageToUser(language, "DefaultAnswers.INTERNAL_ERROR");
                return IntentResponse.HANDLED;
            }
            new Thread(() -> {
                try {
                    Thread.sleep(6000);
                    getOutputChannel().sendRawMessageToUser(Language.TURKISH, "hey asista, merhaba, kaç yaşındasın");
                } catch (final InterruptedException e) {
                    // nothing to do
                }
            }).start();
            getOutputChannel().sendMessageToUser(language, "UserAuthentication.StartEnrollment", Collections.singletonMap("user", this.enrollingUser));
            setState(DialogState.ENROLLING);
            return IntentResponse.HANDLED;
        } else if("AbortEnrollment".equals(action)) {
            setState(DialogState.GOODBYE);
            this.userController.deleteUser(this.enrollingUser);
            getOutputChannel().sendMessageToUser(language, "UserAuthentication.AbortEnrollment", Collections.singletonMap("user", this.enrollingUser));
            this.enrollingUser = null;
            return IntentResponse.HANDLED;
        } else if("CompleteEnrollment".equals(action)) {
            setState(DialogState.GOODBYE);
            getOutputChannel().sendMessageToUser(language, "UserAuthentication.CompleteEnrollment", Collections.singletonMap("user", this.enrollingUser));
            this.enrollingUser = null;
            return IntentResponse.HANDLED;
        } else {
            LOG.warn("Action code missing");
            return IntentResponse.NOT_HANDLED;
        }
    }

    private void notifyAsrComponent() throws IOException {
        // file is read by asr component to start the training procedure
        final String programDataDirectory;
        try {
            programDataDirectory = Configuration.getInstance().getProgramDataDirectory();
        } catch(final ConfigurationException e) {
            LOG.warn(e.getMessage(), e);
            return;
        }
        final String enrollmentFile = programDataDirectory + "enrollment.txt";
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(enrollmentFile), StandardCharsets.UTF_8)) {
            assert this.enrollingUser != null;
            writer.append(String.valueOf(this.enrollingUser.getId())).append("\n");
        } catch (final FileNotFoundException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private static User generateNewUser(@Nonnull final List<User> userList, @Nonnull final Language language) {
        int count = 0;
        String username;
        do {
            count++;
            username = "User "+ count;
        } while(findUsername(username.toLowerCase(), userList));
        final User user = new User();
        user.setName(username);
        user.addLanguageToSpokenLanguages(language.getLangCode3());
        return user;
    }

    private static boolean findUsername(@Nonnull final String username, @Nonnull final List<User> userList) {
        for (final User user : userList) {
            if(username.equals(user.getName())) {
                return true;
            }
        }
        return false;
    }

}
