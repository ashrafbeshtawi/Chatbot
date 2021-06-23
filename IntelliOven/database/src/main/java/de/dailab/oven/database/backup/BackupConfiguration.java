package de.dailab.oven.database.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.exceptions.ConfigurationException;
/**
 * Configuration data needed to handle backup plan.<br>
 * Backup plan includes maximal two backup files.
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class BackupConfiguration {
	@Nonnull
	private static final String SEP = File.separator;
	@Nonnull 
	private static final String NEO4J_PATH = SEP +"var" + SEP + "lib" + SEP + "neo4j" + SEP;
	@Nonnull
	private static final String DATABASE_PATH = NEO4J_PATH + "data" + SEP + "graph.db";
	@Nonnull
	private static final String BACKUP_DIRECTORY = NEO4J_PATH + "import" + SEP;
	@Nonnull
	private static final String CURRENT_BACKUP_NAME = "backUpGraph.graphml";
	@Nonnull
	private static final String CURRENT_BACKUP_FILE_PATH = BACKUP_DIRECTORY + CURRENT_BACKUP_NAME;
	@Nonnull
	private static final String PRIOR_BACKUP_NAME = "backUpGraph_old.graphml";
	@Nonnull
	private static final String PRIOR_BACKUP_FILE_PATH = BACKUP_DIRECTORY + PRIOR_BACKUP_NAME;
	private static final long MILLISECONDS_UNTIL_BACKUP_EXPIRES = TimeUnit.HOURS.toMillis(24);	
	private static final long REGULAR_MILLISECONDS_TO_SLEEP = TimeUnit.HOURS.toMillis(1);
	@Nonnull
	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	@Nonnull
	private static final Logger LOGGER = Logger.getGlobal();
	@Nullable
	private DatabaseConfiguration databaseConfiguration;
	private boolean osAndConfigurationAccepted = false;
	@Nonnull
	private static final List<String> BACKUP_FILE_PATHS = Stream.of(CURRENT_BACKUP_FILE_PATH, 
			PRIOR_BACKUP_FILE_PATH).collect(Collectors.toList());
	
	@Nullable
    private static BackupConfiguration instance;
	
	/**
	 * Ensures that the configuration is valid. (The system is running on Raspbian distribution) <br>
	 * If it is valid the boolean <i>osAndConfigurationAccepted</i> is set true
	 */
	private BackupConfiguration () {
		//Checks if the current operating system is a Linux distribution, since upcoming system 
		//commands would not be runnable if not 
		final boolean raspbian = isRaspbian();
		final boolean validConfig = isValidConfiguration();
		final boolean sudoPermission = sudoPermission();
		
		if(OS_NAME.contains("linux") && raspbian && validConfig && sudoPermission)
			this.osAndConfigurationAccepted = true;
			
		else {
			final String message = String.format("%s is not supported for backup handling: Raspbian: %b,"
					+ " valid config: %b, sudo permission: %b", 
					OS_NAME, raspbian, validConfig, sudoPermission);
		
			LOGGER.log(Level.SEVERE, message);
		}
	}
	
	/**
	 * @return The instance of the class.
	 */
	@Nonnull
    public static BackupConfiguration getInstance() {
        synchronized (BackupConfiguration.class) {
            if (instance == null) instance = new BackupConfiguration();
        }
        return instance;
    }
	
    /**
     * @return The databases file path.
     */
	@Nonnull
    public String getDatabasePath() {
    	return DATABASE_PATH;
    }
    
    /**
     * @return The current backups name.
     */
	@Nonnull
    public String getCurrentBackupName() {
    	return CURRENT_BACKUP_NAME;
    }
    
    /**
     * @return The backup directory path.
     */
	@Nonnull
    public String getBackupDirectory() {
    	return BACKUP_DIRECTORY;
    }
    
    /**
     * @return The current backups file path.
     */
	@Nonnull
    public String getCurrentBackupFilePath() {
    	return CURRENT_BACKUP_FILE_PATH;
    }
    
	/**
	 * @return The prior backups name.
	 */
	@Nonnull
    public String getPriorBackupName() {
    	return PRIOR_BACKUP_NAME;
    }
	
	/**
	 * @return The prior backups file path.
	 */
	@Nonnull
	public String getPriorBackupFilePath() {
		return PRIOR_BACKUP_FILE_PATH;
	}
	
	/**
	 * @return The time when a backup expires in milliseconds.
	 */
	public long getMilliSecondsUntilBackupExpires () {
		return MILLISECONDS_UNTIL_BACKUP_EXPIRES;
	}
	
	/**
	 * @return The time how long the thread should sleep in case everything is fine.
	 */
	public long getRegularMilliSecondsToSleep () {
		return REGULAR_MILLISECONDS_TO_SLEEP;
	}
	
	/**
	 * @return The operating systems name.
	 */
	@Nonnull
	public String getOperatingSystemName() {
		return OS_NAME;
	}
	
	/**
	 * @return The database configuration if it is valid.<br><strong>null</strong> otherwise.
	 */
	@Nullable
	public DatabaseConfiguration getDatabaseConfiguration() {
		return this.databaseConfiguration;
	}
	
	/**
	 * @return <strong>true</strong> if the operating system and the configuration is accepted.<br><strong>false</strong> if not.
	 */
	public boolean getOperatingSystemIsAccepted() {
		return this.osAndConfigurationAccepted;
	}
	
	/**
	 * @return The list of the backup file pathsi 
	 */
	public List<String> getBackupFilePaths() {
		return BACKUP_FILE_PATHS;
	}
	
	/**
	 * @return True if sudo permission is given<br>False if not
	 */
	private boolean sudoPermission () {
		try {
			Process process = Runtime.getRuntime()
					.exec("sudo cp -a /var/lib/neo4j/import/ /var/lib/neo4j/import2/");
			
			process.destroy();
			
			process = Runtime.getRuntime().exec("sudo rm -rf /var/lib/neo4j/import2/");
			
			process.waitFor();
			process.destroy();

			return true;
			
		} 
		catch (final InterruptedException e) {Thread.currentThread().interrupt();}
		catch (final Exception e2) {
			LOGGER.log(Level.WARNING, "Sudo permission not given, "
					+ "BackupHandler will not start working.");
		}
		return false;
	}
	
	/**
	 * @return True if database configuration is valid<br>False otherwise
	 */
	private boolean isValidConfiguration() {
		try {
			//Try to load the databases configuration, catch the eventually occurred exception and
			//just log it since it is going to be handled within the controller
			this.databaseConfiguration = Configuration.getInstance().getDatabaseConfiguration();
			//When it is loaded, double check if the given URI refers to a local graph, 
			//if not Log the error 
			if(this.databaseConfiguration.getUri().contains("localhost") 
					|| this.databaseConfiguration.getUri().contains("127.0.0.1"))
				
				return true;

			else
				LOGGER.log(Level.SEVERE, "No local graph referenced. "
						+ "Backups can just be created for local graphs. Current Graph-IP: {0}",
						this.databaseConfiguration.getUri());
			
		} catch (final ConfigurationException | NullPointerException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
		}
		
		return false;
	}
	
	/**
	 * @return True if operating system is a Raspian one<br>False otherwise
	 */
	private boolean isRaspbian() {
		try {
			
			//Further check if the distribution is a Raspbian one
			final Process process = Runtime.getRuntime().exec("cat /etc/issue");
			
			final BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			final boolean raspbian = bufferedReader.readLine().toLowerCase().contains("raspbian");
			
			process.waitFor();
			process.destroy();
			//If not, log the information that the operating system is not supported
			return raspbian; 
	
		} 
		catch (final InterruptedException e) {Thread.currentThread().interrupt();}
		catch (final Exception e2) {return false;}
		
		return false;
	}
}