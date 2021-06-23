package de.dailab.oven.database.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.Nonnull;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.oven.database.exceptions.ConfigurationException;
import zone.bot.vici.Language;

public class Configuration
{

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String CONFIG_FILENAME = "config.json";

    private static Configuration instance;
    @Nonnull
    private static final String PROGRAM_DATA_DIRECTORY = System.getProperty("user.home") + File.separator
            + ".javaProgramData" + File.separator + "IntelliOven" + File.separator;
    private final JSONObject configurationData;

    private Configuration(final JSONObject data) {
        this.configurationData = data;
        final File programDataDir = new File(PROGRAM_DATA_DIRECTORY);
        if(!programDataDir.exists()) {
            programDataDir.mkdirs();
        }
    }

    public DatabaseConfiguration getDatabaseConfiguration() throws ConfigurationException
    {
        return getDatabaseConfiguration("remoteGraph");
    }

    /**
     * Returns the path of the folder in which addition data of the program can be stored.
     *
     * @return path of data folder
     */
    public String getProgramDataDirectory() {
        return PROGRAM_DATA_DIRECTORY;
    }

    public Language getLanguage() {
        return Language.getLanguage(this.configurationData.getString("language"));
    }

    private DatabaseConfiguration getDatabaseConfiguration(final String objectName) throws ConfigurationException
    {
        ensureConfigurationLoaded();
        final JSONObject config = this.configurationData.getJSONObject(objectName);
        return new DatabaseConfiguration(
                config.getString("uri"),
                config.getString("user"),
                config.getString("password")
        );
    }

    private void ensureConfigurationLoaded() throws ConfigurationException
    {
        if(this.configurationData == null)
        {
            loadConfiguration();
        }
    }

    public static Configuration getInstance() throws ConfigurationException {
        synchronized (Configuration.class) {
            if (instance == null) {
                instance = new Configuration(loadConfiguration());
            }
        }
        return instance;
    }

    private static JSONObject loadConfiguration() throws ConfigurationException
    {
        final StringBuilder jsonBuilder = new StringBuilder();
        final File userDirectoryFile = new File(System.getProperty("user.home"), CONFIG_FILENAME);
        final File workingDirectoryFile = new File(CONFIG_FILENAME);
        InputStream is = null;
        if(workingDirectoryFile.exists()) {
            try {
                LOG.info("Loading config file from working directory: {}", workingDirectoryFile.getAbsolutePath());
                is = new FileInputStream(workingDirectoryFile);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException("Internal Error. Config file in working directory shall exist but was not found!", e);
            }
        }
        else if(userDirectoryFile.exists()) {
            try {
                LOG.info("Loading config file from user directory: {}", userDirectoryFile.getAbsolutePath());
                is = new FileInputStream(userDirectoryFile);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException("Internal Error. Config file in user directory shall exist but was not found!", e);
            }
        }
        else {
            LOG.info("Loading default config file from resources...");
            final ClassLoader classLoader = Configuration.class.getClassLoader();
            is = classLoader.getResourceAsStream(CONFIG_FILENAME);
        }

        if(is == null) {
            throw new ConfigurationException("Could not find a configuration file");
        }
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            reader.lines().forEach(jsonBuilder::append);
        }
        catch (final IOException e) {
            throw new ConfigurationException(e);
        }
        return new JSONObject(jsonBuilder.toString());
    }

}
