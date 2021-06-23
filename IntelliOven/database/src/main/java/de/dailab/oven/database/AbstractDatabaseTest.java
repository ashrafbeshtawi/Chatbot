package de.dailab.oven.database;

import javax.annotation.Nonnull;

import org.junit.Assume;
import org.junit.Before;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;

/**
 * Provides a {@link Graph} instance that can be used for database tests. Credentials need to be set via environment variables otherwise tests will be skipped.
 *
 * @author Hendrik Motza
 * @since 19.11
 */
public abstract class AbstractDatabaseTest {

    @Nonnull
    private static final String ENVKEY_URI = "oven_database_test_uri";
    @Nonnull
    private static final String ENVKEY_USER = "oven_database_test_user";
    @Nonnull
    private static final String ENVKEY_PW = "oven_database_test_pw";

    private Graph testGraph;

    @Before
    public void init() throws Throwable {
        String uri = System.getenv(ENVKEY_URI);
        String user = System.getenv(ENVKEY_USER);
        String pw = System.getenv(ENVKEY_PW);
        
        if(uri == null || user == null || pw == null) {
        	uri = System.getProperty(ENVKEY_URI);
        	user = System.getProperty(ENVKEY_USER);
        	pw = System.getProperty(ENVKEY_PW);
        }

        Assume.assumeTrue(uri != null && user != null && pw != null);
        this.testGraph = new Graph(new DatabaseConfiguration(uri, user, pw));
        initialize();
    }
    
    public abstract void initialize() throws Throwable;

    public Graph getGraph() {
        return this.testGraph;
    }

}
