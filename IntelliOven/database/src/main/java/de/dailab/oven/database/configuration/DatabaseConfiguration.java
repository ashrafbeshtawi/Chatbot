package de.dailab.oven.database.configuration;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;

/**
 * Specifies credentials that are required to connect to the database.
 *
 * @author Hendrik Motza
 * @since 19.11
 */
public class DatabaseConfiguration {

    @Nonnull
    private final String uri;

    @Nonnull
    private final AuthToken authToken;

    @Nullable
    private Configuration configuration;
    
    /**
     * Create new configuration settings for database connection.
     *
     * @param uri The uri that is needed to connect to database.
     * @param username to connect to database
     * @param password to connect to database
     */
    public DatabaseConfiguration(@Nonnull final String uri, @Nonnull final String username, @Nonnull final String password) {
        this.uri = Objects.requireNonNull(uri, "Parameter 'uri' must not be empty.");
        Objects.requireNonNull(username, "Parameter 'username' must not be empty.");
        Objects.requireNonNull(password, "Parameter 'password' must not be empty.");
        this.authToken = AuthTokens.basic(username, password);
    }
    
    /**
     * Get URI for connecting to database.
     *
     * @return uri
     */
    @Nonnull
    public String getUri() {
        return this.uri;
    }

    @Nonnull
    AuthToken getAuthToken() {
        return this.authToken;
    }

}
