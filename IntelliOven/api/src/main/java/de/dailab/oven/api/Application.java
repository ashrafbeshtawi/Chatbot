package de.dailab.oven.api;

import de.dailab.oven.controller.DatabaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class Application {

    private static final String[] CONFIG_FILES_LOGGING = {"log4j2.xml", "log4j2.properties", "log4j2.json", "log4j2.yml"};

    private static final String PROPKEY_GIT_BRANCH = "backend.branch";
    private static final String PROPKEY_GIT_COMMIT_ID = "backend.commit.id.abbrev";
    private static final String PROPKEY_BUILD_TIME = "backend.build.time";
    private static final String PROPKEY_BUILD_VERSION = "backend.build.version";
    private static final String DEFAULT = "[Unknown]";

    public static void main(final String[] args) {
        initLoggingAndVersioning();
        SpringApplication.run(Application.class, args);
        DatabaseController.getInstance();
    }

    private static void initLoggingAndVersioning() {
        for (final String filename : CONFIG_FILES_LOGGING) {
            if (new File(filename).exists()) {
                System.setProperty("log4j.configurationFile", filename);
                break;
            }
        }
        final Properties properties = new Properties(System.getProperties());
        try {
            try (final InputStream is = Application.class.getClassLoader().getResourceAsStream("version.properties")) {
                if (is != null) {
                    properties.load(is);
                }
            }
        } catch (final IOException e) {
            LoggerFactory.getLogger(Application.class).warn("Resource file version.properties could not be read.", e);
        }
        final String branch = properties.getProperty(PROPKEY_GIT_BRANCH, DEFAULT);
        System.setProperty(PROPKEY_GIT_BRANCH, branch);
        final String buildTime = properties.getProperty(PROPKEY_BUILD_TIME, DEFAULT);
        System.setProperty(PROPKEY_BUILD_TIME, buildTime);
        final String buildVersion = properties.getProperty(PROPKEY_BUILD_VERSION, DEFAULT);
        System.setProperty(PROPKEY_BUILD_VERSION, buildVersion);
        final String commitId = properties.getProperty(PROPKEY_GIT_COMMIT_ID, DEFAULT);
        System.setProperty(PROPKEY_GIT_COMMIT_ID, commitId);
        final Logger log = LoggerFactory.getLogger(Application.class);
        log.info("Build Version: {}", buildVersion);
        log.info("Build Time: {}", buildTime);
        log.info("Branch: {}", branch);
        log.info("Commit ID: {}", commitId);
    }

}
