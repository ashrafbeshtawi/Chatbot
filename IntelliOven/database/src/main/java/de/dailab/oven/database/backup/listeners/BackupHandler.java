package de.dailab.oven.database.backup.listeners;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;

import de.dailab.oven.database.backup.BackupConfiguration;
import de.dailab.oven.database.backup.events.BackupFileDateRevisorEvent;
import de.dailab.oven.database.backup.events.BackupFileNumberRevisorEvent;
import de.dailab.oven.database.backup.events.CreateBackupEvent;
import de.dailab.oven.database.backup.events.DatabaseConnectionEvent;
import de.dailab.oven.database.backup.events.WaitEvent;
import de.dailab.oven.database.backup.threads.KillStateThread;
import de.dailab.oven.database.backup.tools.ThreadStarter;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.DatabaseException;

/**
 * Backup handler which handles backup files as well as ensures the database being alive and initiates failure states if backups can not be created or the database can not be recovered.<br>
 * <strong>Note</strong> that APOC Services for Neo4J-Graph must be enabled!<br>
 * <strong>Note</strong> that this backup is made for offline databases on localhost!<br>
 * <strong>Note</strong> that the databases configuration needs to be stored within the configuration.json file.<br> 
 * <strong>Note</strong> JUST PROGRAMMED FOR DEBIAN<br>
 * @author Tristan Schroer
 * @since 04.02.2020
 */
public class BackupHandler implements DatabaseListener{
	

	//Randomly chosen ID to more likely prevention of falsely handling events from other classes
	private static final int ID = 417549817;

	//Internal status codes for correct handling
	//Noncritical status codes
	private static final int REGULAR 							= ID + 1;
	private static final int FIRST_EXECUTION 					= ID + 3;
	private static final int START_CONNECTING 					= ID + 8;
	private static final int CREATE_BACKUP 						= ID + 12;
	private static final int ENSURE_CREATED_BACKUP_EXISTS 		= ID + 18;
	
	//Semi-critical status codes 
	private static final int LOADED_BACKUP 						= ID + 31;
	private static final int FAILED_TO_CREATE_BACKUP 			= ID + 45;
	private static final int RETRY_TO_CREATE_BACKUP 			= ID + 65;
	private static final int RETRY_CONNECTING 					= ID + 81;
	private static final int RECHECK_BACKUP_FILE_NUMBER 		= ID + 103;
	
	//Critical status codes
	private static final int LOADING_BACKUP 					= ID + 124;
	private static final int LOAD_BACKUP 						= ID + 149;
	private static final int STOP_NEO4J 						= ID + 173;
	private static final int START_NEO4J 						= ID + 239;
	private static final int LOADING_BACKUP_FAILED 				= ID + 271;
	private static final int CONNECTION_LOST 					= ID + 300;
	private static final int CONNECTION_LOST_NO_BACKUP 			= ID + 326;
	private static final int FINALLY_FAILED_TO_CREATE_BACKUP 	= ID + 382;
	private static final int CHECKING_FAILED					= ID + 456;
	
	//Sensitive status code
	private static final int CLEANING_DATABASE	 				= ID + 544;

	@Nonnull
	private static final BackupConfiguration B_CONFIG = BackupConfiguration.getInstance();
	private boolean startWorking = B_CONFIG.getOperatingSystemIsAccepted();
	private boolean checkedForEmptyDatabase = false;
	@Nullable
	private CriticalDatabaseStateListener criticalStateListener = null;
	private int numberOfBackupFilesCode = -1;
	@Nonnull
	private static final Logger LOGGER = Logger.getGlobal();
	@Nullable
	private Graph graph = null;
	@Nonnull
	private LocalDateTime nextDatabaseCheck = LocalDateTime.now();
	@Nonnull
	private LocalDateTime nextBackupFileCheck = LocalDateTime.now();
	@Nonnull
	private static final ThreadStarter STARTER = new ThreadStarter();
	@Nullable
    private static BackupHandler instance;
	@Nonnull 
	private static final String SUDO_REMOVE = "sudo rm -rf ";
	private int currentStatusCode = FIRST_EXECUTION;
	
	/**
	 * Initialize the backup handler, which automatically starts two threads for checking backup files 
	 * in case operating system and configuration fulfill the backup handlers needs. <br>
	 * To make the backup handler start call <i>.startHandling()</i> after initialization.
	 */
	private BackupHandler() {	
		//Do just start handling, if configuration and operating system are accepted
		if(!this.startWorking) return;
		
		//Get a number code giving evidence about how many backup files exist. Further handling in 
		//gotBackupFileNumberCode(FIRST_EXECUTION)
		STARTER.startBackupFileNumberRevisorThread(FIRST_EXECUTION, this);
		
		//Check if backup files are up to date if they exist. Further handling in backupFilesUpToDate
		//(FIRST_EXECUTION) and backupFilesNotUpToDate(FIRST_EXECUTION) 
		STARTER.startBackupFileDateRevisorThread(FIRST_EXECUTION, this);
		
		//Log status code
		logStatus();
	}
	
	/**
	 * Initializes the BackupHandler if necessary
	 * @return The synchronized instance of the BackupHandler
	 */
    public static BackupHandler getInstance() {
    	
        synchronized (BackupHandler.class) {
            if (instance == null) instance = new BackupHandler();
        }
        return instance;
    }
	
    /**
     * Sets the CriticalDatabaseStateListener for all operations
     * @param listener The CriticalDatabaseStateListener to set
     */
    public void setCriticalDatabaseStateListener(
    		@Nullable final CriticalDatabaseStateListener listener) {
    	
    	this.criticalStateListener = listener;
    }
    
    /**
     * @return The currently set CriticalDatabaseStateListener
     */
    public CriticalDatabaseStateListener getCriticalDatabaseStateListener() {
    	return this.criticalStateListener;
    }
    
    /**
     * @return True if BackupHandler is working<br>False otherwise
     */
    public boolean isWorking() {return this.startWorking;}
    
	/**
	 * Procedure which causes the start of ensuring database and backup files are available.
	 */
	public void startHandling() {
		//Do just start handling, if configuration and operating system are accepted
		if(this.startWorking) {
			
			//Switching on current status code to avoid handling older states
			switch(this.currentStatusCode) {
			
			//Ensure waiting for initialization of all components and services (waits 5 minutes). 
			//Further handling in waitingSucceeded(FIRST_EXECUTION)
			case FIRST_EXECUTION:
				STARTER.startDatabaseWaitThread(TimeUnit.MINUTES.toMillis(5), 
						FIRST_EXECUTION, LocalDateTime.now().plusMinutes(5), this);
				break;
			
			//Called when first execution state passed
			case REGULAR:
				//Ensures connection to graph. Further handling in connectionToDatabaseSucceeded()
				//and connectionToDatabaseFailed().
				//Will be executed when called for the first time, since the first check is set to 
				//the time when the class has been initialized. 
				if(LocalDateTime.now().isAfter(this.nextDatabaseCheck)) connectToGraph(REGULAR);
				

				//Double check if BackupFileNumberRevisorThread already finished, start a new one 
				//and block until it has finished. Further handling in 
				//gotBackupFileNumberCode(RECHECK_BACKUP_FILE_NUMBER)
				else if(this.numberOfBackupFilesCode < 0){

					this.currentStatusCode = RECHECK_BACKUP_FILE_NUMBER;
					
					//Log status code
					logStatus();
					
					STARTER.startBackupFileNumberRevisorThread(RECHECK_BACKUP_FILE_NUMBER, this);
				}		
				
				//Check if database is empty, which might happen if system has been restarted
				//during loading a backup file
				else if(!this.checkedForEmptyDatabase) checkForEmptyDatabase();
				
				//0 and 2 as code mean that there is no current backup file 
				//(can happen if the file has been broken). Further handling in 
				//backupQueryExecuted(CREATE_BACKUP), backupQueryFailed(CREATE_BACKUP)
				//or connectionToDatabaseFailed(CREATE_BACKUP)
				else if(this.numberOfBackupFilesCode == 0 || this.numberOfBackupFilesCode == 2)
					STARTER.startBackupFileCreatorThread(CREATE_BACKUP, this.graph, this);
				
				
				
				//If there is a current backup file, check if it is up to date (Further actions 
				//handled based on the result in backupFilesUpToDate(REGULAR) 
				//and backupFilesNotUpToDate(REGULAR) 
				else if(LocalDateTime.now().isAfter(this.nextBackupFileCheck))
					STARTER.startBackupFileDateRevisorThread(REGULAR, this);
										

				//If everything is fine, start waiting for another hour to check again. 
				//Further handling in waitingSucceeded(REGULAR)
				else {
					this.nextDatabaseCheck = LocalDateTime.now().plusHours(1);
					
					STARTER.startDatabaseWaitThread(B_CONFIG.getRegularMilliSecondsToSleep(), 
							REGULAR, LocalDateTime.now().plusHours(1), this);
				}
					
				
				break;
			
			//Do nothing in any other case
			default:
				break;
			}
		}
	}
    
	/**
	 * Deletes the old database completely to let Neo4J do the job of creating a clean database instance.<br>
	 * Furthermore possibly existing backup files will be deleted as well.<br>
	 * <strong>Note</strong> that this operation can not be undone!
	 */
	public void cleanDatabase() {
		if(this.startWorking) {
			this.currentStatusCode = CLEANING_DATABASE;
			
			//Log status code
			logStatus();
			
			//Stop the Neo4J service - further handling in waitingSuceeded(CLEANING_DATABASE)
			stopNeo4J(STOP_NEO4J);
			
			//Remove backup files as well as the databases directory
			executeSystemCommand(SUDO_REMOVE + B_CONFIG.getCurrentBackupFilePath(), 
					CLEANING_DATABASE); 
			
			executeSystemCommand(SUDO_REMOVE + B_CONFIG.getPriorBackupFilePath(), 
					CLEANING_DATABASE); 
			
			executeSystemCommand(SUDO_REMOVE + B_CONFIG.getDatabasePath(), 
					CLEANING_DATABASE);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void gotBackupFileNumberCode(
			@Nonnull final BackupFileNumberRevisorEvent backupFileNumberRevisorEvent) {
		
		if(!this.startWorking) return; 
			
		//Ensure backupFileNumberRevisorEvent is not null
		final int gottenNumberOfBackupFilesCode = Objects.requireNonNull(backupFileNumberRevisorEvent,
				"BackupFileNumberRevisorEvent must not be NULL").getNumberOfBackupFilesCode();
		
		final int sourceStatusCode = backupFileNumberRevisorEvent.getSourceStatusCode();
		
		switch (sourceStatusCode) {
		
		//Caused by: Constructor(FIRST_EXECUTION)
		case FIRST_EXECUTION:
			//This procedure might be done before the whole program should start handling more 
			//complex actions, hence no further actions except setting the parameter are called.
			this.numberOfBackupFilesCode = gottenNumberOfBackupFilesCode;
			break;
			
		//Caused by: startHandling(REGULAR)
		//Continue handling as usual
		case RECHECK_BACKUP_FILE_NUMBER:

			this.numberOfBackupFilesCode = gottenNumberOfBackupFilesCode;
			//Double check with current status to ensure  
			//no unwanted operations will be caused
			if(this.currentStatusCode == RECHECK_BACKUP_FILE_NUMBER) {
				this.currentStatusCode = REGULAR;
				
				//Log status code
				logStatus();
				
				startHandling();					
			}
			break;
			
		//Caused by connectionToDatabaseFailed RETRYING_TO_CONNECT(REGULAR || FAILED_TO_CREATE_BACKUP))
		//Since connection is lost finally, the backup needs to be loaded 
			//or a kill state initialized
		case CONNECTION_LOST:
			this.numberOfBackupFilesCode = gottenNumberOfBackupFilesCode;
			
			//Double check with current status to ensure  
			//no unwanted operations will be caused
			if(this.currentStatusCode == CONNECTION_LOST) {
				
				//If there is a backup file, start loading it, start KillStateEvent if not
				if(this.numberOfBackupFilesCode > 0) {
					this.currentStatusCode = LOADING_BACKUP;
					
					//Log status code
					logStatus();
					
					loadBackup(LOAD_BACKUP);
				}
				else {
					this.currentStatusCode = REGULAR;
					
					//Log status code
					logStatus();
					
					STARTER.startKillStateThread(KillStateThread.NO_CONNECTION_NO_BACKUP, 
							this.criticalStateListener);
				}				
			}
			break;
			
			
		//Try to ensure the backup query performed well. 
			//In case of uncertainty call BackupFileDateRevisorThread
		//Caused by: backupQueryExecuted(CREATE_BACKUP)
		case ENSURE_CREATED_BACKUP_EXISTS:
		//Caused by: backupQueryExecuted(RETRY_TO_CREATE_BACKUP)
		case RETRY_TO_CREATE_BACKUP:
			checkCreatingBackup(gottenNumberOfBackupFilesCode, sourceStatusCode);
			break;
			
		//Caused by: loadBackup('LOADING_BACKUP_FAILED')
		//The current backup file could not be used to make the database working again, so that
			//it has been deleted
		case LOADING_BACKUP_FAILED:
			handleLoadingBackupFailed(gottenNumberOfBackupFilesCode);
			break;
			
		//If none of the expected status codes is the current one, do nothing
		default:
			break;
		}
	}

	/**
	 * Invokes further handling when backup should have been created
	 * @param gottenNumberOfBackupFilesCode The gotten number of files code
	 * @param sourceStatusCode The events source status code
	 */
	private void checkCreatingBackup(final int gottenNumberOfBackupFilesCode, final int sourceStatusCode) {
		//Double check with current status to ensure  
		//no unwanted operations will be caused
		if(this.currentStatusCode == REGULAR || 
			this.currentStatusCode == FAILED_TO_CREATE_BACKUP) {
			//If actual is lower, the backup file was created. Switch to regular state.
			if(this.numberOfBackupFilesCode < gottenNumberOfBackupFilesCode) {
				//Update parameters
				this.currentStatusCode = REGULAR;
				
				//Log status code
				logStatus();
				
				this.nextDatabaseCheck = LocalDateTime.now().plusHours(1);
				this.nextBackupFileCheck = LocalDateTime.now().plusHours(12);
				this.numberOfBackupFilesCode = gottenNumberOfBackupFilesCode;
				startHandling();
			}
			
			//Check if maximum of file numbers had been reached already. 
			//(No adaption of numberOfFilesCode needed)
			else if(this.numberOfBackupFilesCode == 3 && 
					this.numberOfBackupFilesCode == gottenNumberOfBackupFilesCode)
				
				STARTER.startBackupFileDateRevisorThread(sourceStatusCode, this);
			
			
			//The number code can just be equal (lower is not possible at this state) or greater, 
			//but not greater than 3. Meaning all other options refer to a failure.
			else {
				//Retry to create backup if it has been the first trial, 
				//cause a BackupFailureThread otherwise
				if(sourceStatusCode == ENSURE_CREATED_BACKUP_EXISTS) 
					connectToGraphAndChangeStatus(REGULAR, FAILED_TO_CREATE_BACKUP);			
				
				else 
					//BackupQuery finally failed, so make further handling possible
					connectToGraphAndChangeStatus(REGULAR, FINALLY_FAILED_TO_CREATE_BACKUP);

			}					
		}
	}

	/**
	 * Invokes further handling when loading backup failed
	 * @param gottenNumberOfBackupFilesCode The gotten number of files code
	 */
	private void handleLoadingBackupFailed(final int gottenNumberOfBackupFilesCode) {
		//Double check with current status to ensure  
		//no unwanted operations will be caused
		if(this.currentStatusCode == LOADING_BACKUP_FAILED) {
			this.numberOfBackupFilesCode = gottenNumberOfBackupFilesCode;
			//If there is no backup file anymore, this means, that the database failed 
			//completely and there is no connection available. So initiate a KillStateThread
			if(this.numberOfBackupFilesCode <= 0) {
				this.currentStatusCode = REGULAR;
				
				//Log status code
				logStatus();
				
				STARTER.startKillStateThread(KillStateThread.NO_CONNECTION_NO_BACKUP, 
						this.criticalStateListener);
			}
			//If there is at least one file, try to load it through calling loadBackup()
			else {
				this.currentStatusCode = LOADING_BACKUP;
				loadBackup(LOAD_BACKUP);				
			}					
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void backupFilesUpToDate(final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
		if(this.startWorking) {
			switch(Objects.requireNonNull(backupFileDateRevisorEvent, 
					"BackupFileDateRevisorEvent must not be NULL.").getSourceStatusCode()) {
			
			//Caused by: Constructor(FIRST_EXECUTION)
			case FIRST_EXECUTION:
				//This procedure might be done before the whole program should start handling more complex 
				//actions, hence no further actions except setting the parameter are called.
				this.nextBackupFileCheck = LocalDateTime.now().plusHours(12);
				break;
				
			//Caused by: startHandling(REGULAR)
			//If the backup files are up to date, set next backup up check to + 12 hours and 
				//go back to normal state
			case REGULAR:
				//Double check with current status to ensure correct handling
				if(this.currentStatusCode == REGULAR) {
					this.nextBackupFileCheck = LocalDateTime.now().plusHours(12);
					startHandling();				
				}
				break;
				
			//Caused by: getBackupFileNumberCode(ENSURE_CREATED_BACKUP_EXISTS || RETRY_TO_CREATE_BACKUP) 
			//If backup is not older than 12 hours it is assumed,
				//that the backup file was created successfully
			case ENSURE_CREATED_BACKUP_EXISTS:
			case RETRY_TO_CREATE_BACKUP:
				//Double check with current status to ensure  
				//no unwanted operations will be caused
				if(this.currentStatusCode == REGULAR || 
				this.currentStatusCode == FAILED_TO_CREATE_BACKUP) {
					
					this.currentStatusCode = REGULAR;				
					//Log status code
					logStatus();
					
					this.nextDatabaseCheck = LocalDateTime.now().plusHours(1);
					this.nextBackupFileCheck = LocalDateTime.now().plusHours(12);
					startHandling();
					break;					
				}
				
				break;
				
			//If none of the known status codes is the current one, do nothing
			default:
				break;
			}		
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void backupFilesNotUpToDate(
			@Nonnull final BackupFileDateRevisorEvent backupFileDateRevisorEvent) {
		
		if(this.startWorking) {
			switch(Objects.requireNonNull(backupFileDateRevisorEvent, 
					"BackupFileDateRevisorEvent must not be NULL.").getSourceStatusCode()) {
				
			//Caused by: startHandling(REGULAR)
			//If this has been a regular check, start creating backup
			case REGULAR:
				//Double check with current status to ensure correct handling
				if(this.currentStatusCode == REGULAR) {
					//Since of the state it is needed to check whether there are already backup files and  
					//rename or remove them before creating a backup
					moveBackupFiles();
					
					STARTER.startBackupFileCreatorThread(CREATE_BACKUP, this.graph, this);				
				}
				break;
				
			//Caused by: getBackupFileNumberCode(ENSURE_CREATED_BACKUP_EXISTS)
			//Set the current state to FAILED_TO_CREATE_BACKUP and check for connection to the database 
				//afterwards to check if it is whether a BackupFailure or a KillState
			case ENSURE_CREATED_BACKUP_EXISTS:
				if(this.currentStatusCode == REGULAR)
					connectToGraphAndChangeStatus(REGULAR, FAILED_TO_CREATE_BACKUP); 					
				
				break;
				
			//Caused by: getBackupFileNumberCode(RETRY_TO_CREATE_BACKUP) 
			//Set the current state to FINALLY_FAILED_TO_CREATE_BACKUP and check for connection 
				//to the database afterwards to check if it is whether a BackupFailure or a KillState
			case RETRY_TO_CREATE_BACKUP:
				if(this.currentStatusCode == FAILED_TO_CREATE_BACKUP)
					connectToGraphAndChangeStatus(REGULAR, FINALLY_FAILED_TO_CREATE_BACKUP);			
				
				break;
				
			//If none of the known status codes is the current one, do nothing
			default:
				break;
			}			
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void waitingSucceeded(@Nonnull final WaitEvent waitEvent) {
		if(this.startWorking) {
			final int sourceStatusCode = Objects.requireNonNull(waitEvent, "WaitEvent must not be NULL")
					.getSourceStatusCode();
			
			switch(this.currentStatusCode) {
			
			//Caused by: startHandling(FIRST_EXECUTION)
			case FIRST_EXECUTION:
				//Double check with source status code to ensure correct handling, start regular 
				//handling afterwards
				if(sourceStatusCode == FIRST_EXECUTION) {
					this.currentStatusCode = REGULAR;
					
					//Log status code
					logStatus();
					
					startHandling();
				}
				break;				
				
			//Caused by: startHandling(REGULAR) || connectToGraph()
			case REGULAR:
				switch(sourceStatusCode) {			
				//If Neo4J service is stopped try to start the service again
				case START_CONNECTING + STOP_NEO4J:
				case RETRY_CONNECTING + STOP_NEO4J:
					this.currentStatusCode = REGULAR + START_NEO4J;
					
					//Log status code
					logStatus();
					
					startNeo4J(sourceStatusCode - STOP_NEO4J + START_NEO4J);
					break;
				
				//If none of the known status codes is the current one, do nothing
				default:
					startHandling();
					break;
				}
				break;
							
			case REGULAR + START_NEO4J:
			case CHECKING_FAILED + START_NEO4J:
			case FAILED_TO_CREATE_BACKUP + START_NEO4J:
			case FINALLY_FAILED_TO_CREATE_BACKUP + START_NEO4J:
				switch(sourceStatusCode) {
				//If Neo4J service is stopped try to start the service again
				case START_CONNECTING + START_NEO4J:
				case RETRY_CONNECTING + START_NEO4J:
					connectToGraphAndChangeStatus(sourceStatusCode, this.currentStatusCode - START_NEO4J);
					break;
					
				default:
					break;
				}

			break;	
				
			case CHECKING_FAILED:
			case FAILED_TO_CREATE_BACKUP:
			case FINALLY_FAILED_TO_CREATE_BACKUP:
				switch(sourceStatusCode) {
				//If Neo4J service is stopped try to start the service again
				case START_CONNECTING + STOP_NEO4J:
				case RETRY_CONNECTING + STOP_NEO4J:
					this.currentStatusCode = this.currentStatusCode + START_NEO4J;
					
					//Log status code
					logStatus();
					
					startNeo4J(sourceStatusCode - STOP_NEO4J + START_NEO4J);
					break;
				default:
					break;
				}
				break;
				
			//Caused by: gotBackupFileNumber(CONNECTION_LOST)->LOADING_BACKUP->loadBackup(LOAD_BACKUP)
			case LOADING_BACKUP:
				handleLoadingBackup(sourceStatusCode);
				break;
					
			//Caused when Neo4J has been started, pass the further actions to loadBackup
			case LOADING_BACKUP + START_NEO4J:
				handleLoadingBackupStartNeo4j(sourceStatusCode);
				break;
				
			//Caused by: cleanDatabase(CLEANING_DATABASE)
			case CLEANING_DATABASE:
				handleCleaningDatabase(sourceStatusCode);
				break;
					
			//If Neo4J service has been started, try to connect to the full clean database
			case CLEANING_DATABASE + START_NEO4J:
				handleCleaningDatabaseStartNeo4J(sourceStatusCode);
				break;
			
									
			//If none of the expected status codes is the current one, do nothing
			default:
				break;
			}		
		}
	}

	/**
	 * Invokes further handling when loading backup has been invoked
	 * @param sourceStatusCode The events source status code
	 */
	private void handleLoadingBackup(final int sourceStatusCode) {
		switch(sourceStatusCode) {
		//Caused when Neo4J has been stopped
		case LOAD_BACKUP + STOP_NEO4J:
			//Remove database so that Neo4J service creates a new one when it is started again
			this.currentStatusCode = LOADING_BACKUP + START_NEO4J;
		
			//Log status code
			logStatus();
		
			if(executeSystemCommand(SUDO_REMOVE + B_CONFIG.getDatabasePath(), 
					LOAD_BACKUP)) {
				startNeo4J(sourceStatusCode - STOP_NEO4J + START_NEO4J);	
			}
		
			break;
			
		case START_CONNECTING + STOP_NEO4J:
		case RETRY_CONNECTING + STOP_NEO4J:
			this.currentStatusCode = LOADING_BACKUP + START_NEO4J;
		
			//Log status code
			logStatus();
			
			startNeo4J(sourceStatusCode - STOP_NEO4J + START_NEO4J);
			break;
		
		//Caused by loadBackup(REGULAR) to ensure query has been executed. Pass further 
		//handling to loadBackup()
		case REGULAR:
			loadBackup(LOADED_BACKUP);
			break;

		//If none of the expected status codes is the current one, do nothing				
		default:
			break;
		}
	}

	/**
	 * Invokes further handling when loading backup has been invoked and start neo4j has been
	 * executed
	 * @param sourceStatusCode The events source status code
	 */
	private void handleLoadingBackupStartNeo4j(final int sourceStatusCode) {
		switch(sourceStatusCode) {
		case LOAD_BACKUP + START_NEO4J:
			this.currentStatusCode = LOADING_BACKUP;
		
			//Log status code
			logStatus();
		
			loadBackup(sourceStatusCode - LOAD_BACKUP);
			
			break;
		
		case START_CONNECTING + START_NEO4J:
		case RETRY_CONNECTING + START_NEO4J:
			connectToGraphAndChangeStatus(sourceStatusCode, LOADING_BACKUP);
			break;
		//If none of the expected status codes is the current one, do nothing
		default:
			break;
		}
	}

	/**
	 * Invokes further handling when cleaning database has been invoked and first wait thread
	 * has been executed
	 * @param sourceStatusCode	The events source status code
	 */
	private void handleCleaningDatabase(final int sourceStatusCode) {
		switch(sourceStatusCode) {
		//If Neo4J service is stopped try to start the service again
		case START_CONNECTING + STOP_NEO4J:
		case RETRY_CONNECTING + STOP_NEO4J:
		case STOP_NEO4J:
			this.currentStatusCode = CLEANING_DATABASE + START_NEO4J;
			
			//Log status code
			logStatus();
			
			startNeo4J(sourceStatusCode - STOP_NEO4J + START_NEO4J);
			break;
		default:
			break;
		}
	}

	/**
	 * Invokes further handling when state of cleaning database is reached and start neo4j 
	 * has been invoked
	 * @param sourceStatusCode The events source status code
	 */
	private void handleCleaningDatabaseStartNeo4J(final int sourceStatusCode) {
		switch(sourceStatusCode) {
		case START_NEO4J:	
			connectToGraphAndChangeStatus(REGULAR, CLEANING_DATABASE);
			break;
			
		case START_CONNECTING + START_NEO4J:
		case RETRY_CONNECTING + START_NEO4J:
			connectToGraphAndChangeStatus(sourceStatusCode, CLEANING_DATABASE);
			break;
			
		//If none of the expected status codes is the current one, do nothing
		default: 
			break;
		}
	}
	
	/**
	 * Invokes connect to graph method and sets the current status beforehand
	 * @param connectStatusCode	The code to pass to the connectToGraphMethod
	 * @param currentStatusCode	The status code to set
	 */
	private void connectToGraphAndChangeStatus(final int connectStatusCode, final int currentStatusCode) {
		this.currentStatusCode = currentStatusCode;
		
		//Log status code
		logStatus();
		
		connectToGraph(connectStatusCode);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void waitingFailed(@Nonnull final WaitEvent waitEvent) {
		if(this.startWorking) {
			final LocalDateTime expirationTime = Objects.requireNonNull(waitEvent,
					"WaitEvent must not be NULL").getExpirationTime();
			//Ensure all procedures waiting at least the intended time and maximal close to 
			//twice the time
			if(LocalDateTime.now().isBefore(expirationTime)) {
				STARTER.startDatabaseWaitThread(waitEvent.getMilliSecondsToWait(), 
						waitEvent.getSourceStatusCode(), expirationTime, this);
			}
			else {
				//The time to wait is over, even with failures which can be ignored. Presume 
				//as if the procedure would have succeeded 
				waitingSucceeded(waitEvent);
			}				
		}
	}
	
	
	/**
	 * Tries to connect to the graph. Retries up to three times where the last two times include 
	 * restarting the Neo4J service
	 * @param privateStatusCode The status code which describes the current try so that different 
	 * actions are derived. <br> 
	 * 			<i>Accepted status codes: <strong>REGULAR, START_CONNECTING, RETRY_CONNECTING, 
	 * START_CONNECTING + START_NEO4J, RETRY_CONNECTING + START_NEO4J</strong></i>
	 */
	private void connectToGraph(final int privateStatusCode) {
		switch (privateStatusCode) {
		//Regular equals the first try of connecting without creating a new instance if not necessary
		case REGULAR:
			//If graph is NULL, a new instance needs to be created within the DatabaseConnectionThread
			if(this.graph == null) {
				STARTER.startDatabaseConnectionThread(privateStatusCode, 
						B_CONFIG.getDatabaseConfiguration(), this);
			}
			
			else {
				//Check if just sessions did expire, if so or the graph is not available, 
				//start the DatabaseConnectionThread
				try(final Session readSession = this.graph.openReadSession()) {
					
					LOGGER.log(Level.INFO, "Graph connection ensured");
					
				} catch(final DatabaseException dE) {
					
					STARTER.startDatabaseConnectionThread(privateStatusCode, 
							B_CONFIG.getDatabaseConfiguration(), 
							this);						
				}
				
			}
			break;

		//Start connecting signals, that trying to create a graph instance failed for the first 
			//time with state Regular and initializes Neo4J Service to restart
		//Retry connecting signals, that trying to create a graph instance failed for the first 
			//time with state Start Connecting and initializes Neo4J Service to restart the second time
		case START_CONNECTING:
		case RETRY_CONNECTING:
			stopNeo4J(privateStatusCode + STOP_NEO4J);
			break;

		//The following state signal, that Neo4J has been restarted so that it is free to try to 
		//open a new graph instance
		case START_CONNECTING + START_NEO4J:
		case RETRY_CONNECTING + START_NEO4J:
			STARTER.startDatabaseConnectionThread(privateStatusCode - START_NEO4J, 
					B_CONFIG.getDatabaseConfiguration(), this);
			break;

		//If none of the expected status codes is the current one, do nothing
		default:
			break;
		}	
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void connectionToDatabaseSucceeded(
			@Nonnull final DatabaseConnectionEvent databaseConnectionEvent) {
		
		if(this.startWorking) {
			
			final Graph tmpGraph = databaseConnectionEvent.getGraph();
			
			//(Re)Set the global graph instance
			if(tmpGraph == null	|| !ensureDatabaseIsWorking(tmpGraph)) {
				connectionToDatabaseFailed(databaseConnectionEvent);
				return;
			}
			
			this.graph = tmpGraph;				
			
			final int sourceStatusCode = databaseConnectionEvent.getSourceStatusCode();
			
			
			//Since establishing connection has been successful, 
			//just continue to perform where the call came from
			//Double check with source status code to ensure correct handling
			if(!isConnectionRelated(sourceStatusCode)) return;
			
			switch(this.currentStatusCode) {
			
			//Initial try to connection without restarting Neo4J service was successfull. 
			//The Regular state signals, that the call came from a regualr handling (startHandling)
			case REGULAR:
				startHandling();					
				break;
				
			//Caused by: loadBackup(START_NEO4J)
			case LOADING_BACKUP:
				loadBackup(REGULAR);					
				break;
				
			//Caused by: backupQueryFailed(CREATE_BACKUP) || connectionToDatabaseFailed(CREATE_BACKUP)
			//Since connection is established, retry to create backup
			case FAILED_TO_CREATE_BACKUP:
				STARTER.startBackupFileCreatorThread(RETRY_TO_CREATE_BACKUP, this.graph, this);
				
				break;
				
			//Caused by: backupQueryFailed(RETRY_TO_CREATE_BACKUP) ||
				// connectionToDatabaseFailed(RETRY_TO_CREATE_BACKUP)
			//Since connection is established but backup file not created initiated BackupFailureThread
			case FINALLY_FAILED_TO_CREATE_BACKUP:
				this.currentStatusCode = REGULAR;
				
				//Log status code
				logStatus();
				
				STARTER.startBackupFailureThread(this.criticalStateListener);

				break;
				
			//Caused by: cleanDatabase(CLEANING_DATABASE)->[...]->waitingSucceeded(CLEANING_DATABASE)
			//Connection could be established 
				//-> Full clean up helped, witch to regular state and start handling
			case CLEANING_DATABASE:

				this.currentStatusCode = REGULAR;
				
				//Log status code
				logStatus();
				
				startHandling();						

				break;
			
			case CHECKING_FAILED:
				//Double check with source status code to ensure correct handling
				checkForEmptyDatabase();
				break;
				
			//If none of the expected status codes is the current one, do nothing
			default:
				break;
			}
			
		}
	}

	/**
	 * Ensures database can perform simple queries
	 * @return True if database is working<br>False otherwise
	 */
	private boolean ensureDatabaseIsWorking(@Nonnull final Graph testGraph) {
		try (final Session readSession = testGraph.openReadSession()){
			//Check for valid transactions
			final StatementResult result = readSession.run(new Statement("MATCH(n) RETURN COUNT(n)"));
			result.list();
			this.nextDatabaseCheck = LocalDateTime.now().plusHours(1);
			return true;
		}	catch (final DatabaseException dE) {
			//If sessions can not be initiated it is the same as if the connection itself could not be established
			return false;
		}	
	}
	
	/**
	 * Returns true is status is connection related
	 * @param sourceStatusCode	The code to test
	 * @return	True if status is connection related<br>False otherwise
	 */
	private boolean isConnectionRelated(final int sourceStatusCode) {
		return (sourceStatusCode == REGULAR 
		|| sourceStatusCode == START_CONNECTING 
		|| sourceStatusCode == RETRY_CONNECTING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void connectionToDatabaseFailed(
			@Nonnull final DatabaseConnectionEvent databaseConnectionEvent) {
		

		if(this.startWorking) {
			final int sourceStatusCode = Objects.requireNonNull(databaseConnectionEvent,
					"DatabaseConnectionEvent must not be NULL").getSourceStatusCode();
			
			switch (sourceStatusCode) {
			//First try for connection failed (without restarting Neo4J service)
			case REGULAR:
			//Caused by: BackupFileCreatorThread(CREATE_BACKUP)
			case CREATE_BACKUP:
			//Cause by: BackupFileCreatorThread(RETRY_TO_CREATE_BACKUP)
			case RETRY_TO_CREATE_BACKUP:
				//If the BackupFileCreatorThread caused this issue switch current status
				if(sourceStatusCode == CREATE_BACKUP) {
					this.currentStatusCode = FAILED_TO_CREATE_BACKUP;
					
					//Log status code
					logStatus();
					
				}
				else if(sourceStatusCode == RETRY_TO_CREATE_BACKUP) {
					this.currentStatusCode = FINALLY_FAILED_TO_CREATE_BACKUP;
					
					//Log status code
					logStatus();
					
				}
				//Try to restart Neo4J service for the first time, set the parameters to null first
				this.graph = null;
				//Retry to connect
				connectToGraph(START_CONNECTING);
				break;
				
			//Second try for connection failed (first try of restarting service)
			case START_CONNECTING:
				connectToGraph(RETRY_CONNECTING);
				break;
				
			//Define what to do if connection has been lost finally 
			//(second try of restarting service)
			case RETRY_CONNECTING:
				switch(this.currentStatusCode) {
				
				//In case it has been a regular check, check for backup files 
				//(To further load backup if file(s) exist)
				case REGULAR:
				case FAILED_TO_CREATE_BACKUP:
				case FINALLY_FAILED_TO_CREATE_BACKUP:
				case CHECKING_FAILED:
					this.currentStatusCode = CONNECTION_LOST;
					
					//Log status code
					logStatus();
					
					//Check if backup files exist to load backup eventually
					STARTER.startBackupFileNumberRevisorThread(CONNECTION_LOST, this);
					break;
					
				//In case even performing a full clean up didn't help initialize KillStateThread 
				//to cause a final error
				case CLEANING_DATABASE:
					STARTER.startKillStateThread(KillStateThread.SYSTEM_RESET_REQUIRED, 
							this.criticalStateListener);
					break;
				
				case LOADING_BACKUP:
					this.currentStatusCode = REGULAR;
					
					//Log status 
					logStatus();
					
					STARTER.startKillStateThread(KillStateThread.ESTABLISHING_CONNECTION_FAILED, 
							this.criticalStateListener);
					break;
					
				//If none of the expected status codes is the current one, do nothing
				default:
					break;
				}
				break;
				
			//If none of the known status codes is the current one, startHandling to eventually 
			//initialize restarts etc.
			default:
				this.nextDatabaseCheck = LocalDateTime.now();
				startHandling();
				break;
			}		
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void backupQueryExecuted(final CreateBackupEvent createBackupEvent) {
		if(this.startWorking) {
			switch (createBackupEvent.getSourceStatusCode()) {
			
			//Caused by: startHandling(REGULAR) || backupFilesNotUpToDate(REGULAR)
			//Since query has been executed, a double check is needed, that the file does indeed exist
			case CREATE_BACKUP:
				if(this.currentStatusCode == REGULAR) {
					STARTER.startBackupFileNumberRevisorThread(ENSURE_CREATED_BACKUP_EXISTS, this);					
				}
				break;
				
			//Caused by: gotBackupFileNumberCode(ENSURE_CREATED_BACKUP_EXISTS) || 
				//connectionToDatabaseSucceeded(FAILED_TO_CREATE_BACKUP) 
			//Since query has been executed, a double check is needed, that the file does indeed exist	
			case RETRY_TO_CREATE_BACKUP:
				if(this.currentStatusCode == FAILED_TO_CREATE_BACKUP) {
					STARTER.startBackupFileNumberRevisorThread(RETRY_TO_CREATE_BACKUP, this);					
				}
				break;
				
			//If none of the expected status codes is the current one, do nothing	
			default:
				break;
			}			
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void backupQueryFailed(final CreateBackupEvent createBackupEvent) {
		if(this.startWorking) {
			switch (createBackupEvent.getSourceStatusCode()) {
			
			//Caused by: startHandling(REGULAR) || backupFilesNotUpToDate(REGULAR)
			//First try failed, so ensure there is a database connection
			case CREATE_BACKUP:
				if(this.currentStatusCode == REGULAR) {
					this.currentStatusCode = FAILED_TO_CREATE_BACKUP;
					
					//Log status code
					logStatus();
					
					connectToGraph(REGULAR);					
				}
				break;
				
			//Caused by: gotBackupFileNumberCode(ENSURE_CREATED_BACKUP_EXISTS) || 
				//connectionToDatabaseSucceeded(FAILED_TO_CREATE_BACKUP)
			//Second try failed, hence set the current status to FINALLy_FAILED_TO_CREATE_BACKUP 
				//and test for connection to database in general
			case RETRY_TO_CREATE_BACKUP:
				//This state signals, that the backup could finally not be created which had nothing 
				//to do with a missing connection. So initialize a failure thread.
				if(this.currentStatusCode == FAILED_TO_CREATE_BACKUP) {					
					this.currentStatusCode = FINALLY_FAILED_TO_CREATE_BACKUP;
					
					//Log status code
					logStatus();
					
					connectToGraph(REGULAR);
				}
				break;
				
			//If none of the expected status codes is the current one, do nothing	
			default:
				break;
			}		
		}
	}

	/**
	 * Tries to stop Neo4J service and waits for the system finishing the command. 
	 * Results handled in waitingSucceed().
	 * @param privateStatusCode The sources status code to handle later actions correctly.
	 */
	private void stopNeo4J(final int privateStatusCode) {
		switch(this.currentStatusCode) {
		
		//Avoid stopping Neo4J in case it currently tried to start again
		case REGULAR + START_NEO4J:
		case CHECKING_FAILED + START_NEO4J:
		case CLEANING_DATABASE + START_NEO4J:
		case LOADING_BACKUP + START_NEO4J:
		case FAILED_TO_CREATE_BACKUP + START_NEO4J:
		case FINALLY_FAILED_TO_CREATE_BACKUP + START_NEO4J:
			break;
		
		//Stop it in any other case
		default:
			if(executeSystemCommand("sudo service neo4j stop", privateStatusCode)) {
				STARTER.startDatabaseWaitThread(TimeUnit.SECONDS.toMillis(30), 
						privateStatusCode, LocalDateTime.now().plusSeconds(30), this);			
			}			
		}
	}
	
	/**
	 * Tries to start Neo4J service and waits for the system finishing the command. 
	 * Results handled in waitingSucceed().
	 * @param privateStatusCode The sources status code to handle later actions correctly.
	 */
	private void startNeo4J(final int privateStatusCode) {
		if(executeSystemCommand("sudo service neo4j start", privateStatusCode)) {
			STARTER.startDatabaseWaitThread(TimeUnit.MINUTES.toMillis(3), 
					privateStatusCode, LocalDateTime.now().plusMinutes(3), this);			
		}
	}
	
	/**
	 * Executes a given system command.
	 * <strong>Note</strong> that sudo rights are required without typing a password
	 * @param command			The command to execute
	 * @param privateStatusCode	The sources status code to handle later actions correctly
	 * @return <strong>True</strong>if command has been executed<br><strong>False</strong> if not
	 */
	private boolean executeSystemCommand(@Nullable final String command, final int privateStatusCode) {
		if(command != null && !command.isEmpty()) {
			try {
				final Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
				process.destroy();
			} 
			catch(final InterruptedException e) {Thread.currentThread().interrupt();}
			catch (final IOException e2) {
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(5));
					final Process process2 = Runtime.getRuntime().exec(command);
					process2.waitFor();
					process2.destroy();
				} 
				catch(final InterruptedException e) {Thread.currentThread().interrupt();}
				catch (final IOException e3) {
					
					LOGGER.log(Level.WARNING, "Command '" + command + "' could not be executed.");					
					
					executionFailed(privateStatusCode);
													
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Invokes methods based on the passed status code and should be called in case executing
	 * system command failed
	 * @param privateStatusCode The original passed status code
	 */
	private void executionFailed(final int privateStatusCode) {
		switch(privateStatusCode) {
		
		//Skip restarting and try again without
		case START_CONNECTING + STOP_NEO4J:
		case RETRY_CONNECTING + STOP_NEO4J:
			connectToGraph(privateStatusCode - STOP_NEO4J + START_NEO4J);
			break;
			
		//In case even restarting hasn't been successful
		case CLEANING_DATABASE:
		//In case it is tried to start or stop Neo4J
		case START_NEO4J:
		case STOP_NEO4J:
			stopWorking();
			break;
		
		case START_CONNECTING + START_NEO4J:
		case RETRY_CONNECTING + START_NEO4J:
			switch(this.currentStatusCode) {
			
			case CLEANING_DATABASE + START_NEO4J:
				stopWorking();							
				break;
			
			case LOADING_BACKUP + START_NEO4J:
				establishingConnectionFailed();	
				break;
				
			case REGULAR + START_NEO4J:
			case CHECKING_FAILED + START_NEO4J:
			case FAILED_TO_CREATE_BACKUP + START_NEO4J:
			case FINALLY_FAILED_TO_CREATE_BACKUP + START_NEO4J:
				this.currentStatusCode = CONNECTION_LOST;
				//Log Status
				logStatus();
				STARTER.startBackupFileNumberRevisorThread(CONNECTION_LOST, this);
				break;
				
			default:
				break;
			}
		
			break;
			
		case LOAD_BACKUP:
		case LOAD_BACKUP + STOP_NEO4J:
		case LOAD_BACKUP + START_NEO4J:
		case LOADED_BACKUP:
			//In case loading backup is called, there is no connection available. 
			//Start killStateThread since it is not even possible to try to reestablish 
			//Connection
			establishingConnectionFailed();	
			break;
		
		default:
			break;
		}
	}

	/**
	 * Initializes a KillStateThread which signals that establishing connection failed and sets the
	 * current status to REGULAR to enable further working
	 */
	private void establishingConnectionFailed() {
		if(this.currentStatusCode == LOADING_BACKUP 
				|| this.currentStatusCode == LOADING_BACKUP + START_NEO4J) {
			
			STARTER.startKillStateThread(KillStateThread.ESTABLISHING_CONNECTION_FAILED, 
					this.criticalStateListener);
			this.currentStatusCode = REGULAR;
			
			//Log Status
			logStatus();
		}
	}

	/**
	 * Stops the program working completely
	 */
	private void stopWorking() {
		if(this.currentStatusCode == CLEANING_DATABASE
			|| this.currentStatusCode == CLEANING_DATABASE + START_NEO4J) {
			
			STARTER.startKillStateThread(KillStateThread.SYSTEM_RESET_REQUIRED, 
					this.criticalStateListener);
			
			this.startWorking = false;
		}
	}
	
	/**
	 * Tries to load backup files, most up to date first. In case of failure the backup file is 
	 * deleted and the next one is tried to load into the database.
	 * @param statusCode	A status code to derive next actions
	 */
	private void loadBackup(final int statusCode) {
		final String backupFilePathOfInterest = getCurrentBackupFilePath();
		
		//Start loading the identified backup file into the database
		if(!backupFilePathOfInterest.isEmpty()) {
			
			switch(statusCode) {
			//Load backup means the initialization of this function. Stop Neo4J first. 
			//In waitingSucceeded(LOAD_BACKUP + STOP_NEO4J) the databases file path is 
			//deleted and Neo4J started afterwards
			case LOAD_BACKUP:
				stopNeo4J(LOAD_BACKUP + STOP_NEO4J);
				break;
				
			//Neo4J has been started, so try to connect to the graph
			case START_NEO4J:
				connectToGraph(REGULAR);
				break;
				
			//Connection has been established, so execute the query and wait for it to finish
			case REGULAR:
				if(performLoadBackupQuery(backupFilePathOfInterest))
					
					STARTER.startDatabaseWaitThread(TimeUnit.MINUTES.toMillis(1), REGULAR, 
							LocalDateTime.now().plusMinutes(1), this);
				else
					deleteBackupAfterLoading(backupFilePathOfInterest);
				
				break;
				
			//Backup has been loaded without obvious failures. Ensure correctness through counting nodes 
				//(An empty graph will not be exported by default in Neo4J)
			case LOADED_BACKUP:
				
				final Statement checkStatement = new Statement("MATCH(n) RETURN count(n)");
				
				try (final Session readSession = this.graph.openReadSession()){
					final StatementResult result = readSession.run(checkStatement);
					
					//Check if result is NULL or 0, if so, the backup failed. Delete the backup file and try 
					//to load another one. If not, loading backup was successfull, so reset all parameters 
					//and go back to regular state
					if(!result.hasNext() || result.list().get(0).get(0).asInt() == 0) 
						deleteBackupAfterLoading(backupFilePathOfInterest);
					
					else {
						this.currentStatusCode = REGULAR;
						
						//Log status code
						logStatus();
						
						this.nextDatabaseCheck = LocalDateTime.now().plusHours(1);
						this.nextBackupFileCheck = LocalDateTime.now().plusHours(12);
						startHandling();
					}		
					
				} catch(final Exception e) {deleteBackupAfterLoading(backupFilePathOfInterest);}
				
				break;
			
			//If none of the known status codes is the current one, do nothing	
			default:
				break;
			}
			
		}
								
	}
	
	//Checks if database is empty
	private void checkForEmptyDatabase() {
		this.checkedForEmptyDatabase = true;

		try (final Session readSession = this.graph.openReadSession()){
			
			final StatementResult result = readSession.run(new Statement("MATCH(n) RETURN count(n)"));
			final int numberOfNodes = result.list().get(0).get(0).asInt();
			if (this.currentStatusCode == CHECKING_FAILED) {
				//Reset current status code to avoid deadlock
				this.currentStatusCode = REGULAR;				
				//Log status code
				logStatus();
			}
			
			//In this case, the program has been interrupted during loading backup
			//Load Backup with skipping restarting Neo4J service
			if(numberOfNodes == 0 && this.numberOfBackupFilesCode > 0) {

					this.currentStatusCode = LOADING_BACKUP;
					
					//Log status code
					logStatus();
					
					loadBackup(REGULAR);

			} 
			else 
				startHandling();
			
		} catch (final Exception e) {
			
			switch(this.currentStatusCode) {
			case REGULAR:
				connectToGraph(REGULAR);
				this.currentStatusCode = CHECKING_FAILED;
				
				//Log status code
				logStatus();
				
				break;
				
			case CHECKING_FAILED:
				this.currentStatusCode = LOADING_BACKUP;
				
				//Log status code
				logStatus();
				
				loadBackup(LOAD_BACKUP);
				break;
				
			default:
				break;
			}
		}
	}
	
	/**
	 * @return The current status code as string description
	 */
	private String getCurrentStatus() {
		String status = "";
		switch(this.currentStatusCode) {
		case CHECKING_FAILED:
			status = "CHECKING_FAILED";
			break;
		
		case CHECKING_FAILED + START_NEO4J:
			status = "CHECKING_FAILED->START_NEO4J";
			break;
		
		case CLEANING_DATABASE:
			status = "CLEANING_DATABASE";
			break;
			
		case CLEANING_DATABASE + START_NEO4J:
			status = "CLEANING_DATABASE->START_NEO4J";
			break;
			
		case CONNECTION_LOST:
			status = "CONNECTION_LOST";
			break;
			
		case CONNECTION_LOST_NO_BACKUP:
			status = "CONNECTION_LOST_NO_BACKUP";
			break;
			
		case FAILED_TO_CREATE_BACKUP:
			status = "FAILED_TO_CREATE_BACKUP";
			break;
		
		case FAILED_TO_CREATE_BACKUP + START_NEO4J:
			status = "FAILED_TO_CREATE_BACKUP->START_NEO4J";
			break;
		
		case FINALLY_FAILED_TO_CREATE_BACKUP:
			status = "FINALLY_FAILED_TO_CREATE_BACKUP";
			break;
			
		case FINALLY_FAILED_TO_CREATE_BACKUP + START_NEO4J:
			status = "FINALLY_FAILED_TO_CREATE_BACKUP->START_NEO4J";
			break;
		
		case FIRST_EXECUTION:
			status = "FIRST_EXECUTION";
			break;
			
		case LOADING_BACKUP:
			status = "LOADING_BACKUP";
			break;
			
		case LOADING_BACKUP + START_NEO4J:
			status = "LOADING_BACKUP->START_NEO4J";
			break;
			
		case LOADING_BACKUP_FAILED:
			status ="LOADING_BACKUP_FAILED";
			break;
			
		case RECHECK_BACKUP_FILE_NUMBER:
			status = "RECHECK_BACKUP_FILE_NUMBER";
			break;
			
		case REGULAR:
			status = "REGULAR";
			break;
			
		case REGULAR + START_NEO4J:
			status = "REGULAR->START_NEO4J";
			break;
			
		default:
			break;
		}
		
		return "BackupHandler-Status: " + status;
	}
	
	/**
	 * Logs the current status
	 */
	private void logStatus() {
		LOGGER.log(Level.INFO, getCurrentStatus());
	}
	
	/**
	 * Moves (and deletes) the backup files if necessary
	 */
	private void moveBackupFiles() {
		switch(this.numberOfBackupFilesCode) {
		case 3:
			executeSystemCommand(SUDO_REMOVE + B_CONFIG.getPriorBackupFilePath(), REGULAR);
			executeSystemCommand("sudo mv -f " + B_CONFIG.getCurrentBackupFilePath() + " " 
					+ B_CONFIG.getPriorBackupFilePath(), REGULAR);
			break;
			
		case 1:
			executeSystemCommand("sudo mv -f " + B_CONFIG.getCurrentBackupFilePath() + " " 
					+ B_CONFIG.getPriorBackupFilePath(), REGULAR);
			break;
		
		//No deleting or moving files required 
		default:
			break;
		}
	}
	
	/**
	 * Identifies which backup file path is the most recent one
	 * @return	Empty String in case there is no backup file<br>File path otherwise
	 */
	private String getCurrentBackupFilePath() {
		//Identify which backup file to load
		switch(this.numberOfBackupFilesCode) {
		//Case 1 and 3 mean, that there is a more up to date file than another one
		case 1:
		case 3:
			return B_CONFIG.getCurrentBackupFilePath();

		//Case 2 means that there is only one, more expired backup file left 
		case 2:
			return B_CONFIG.getPriorBackupFilePath();
		//In case the status code is not one of the mentioned, do nothing
		default:
			return "";
		}
	}
	
	
	/**
	 * Tries to load the given backup into the database and deletes the backup in case of failure
	 * @param backupFilePathOfInterest The backup to load into the database
	 * @return True if backup query has been executed<br>False otherwise
	 */
	private boolean performLoadBackupQuery(final String backupFilePathOfInterest) {
		try (final Session writeSession = this.graph.openWriteSession()){
			
			final Statement statement = new Statement("CALL apoc.import.graphml.all('"
					+ backupFilePathOfInterest + "', {useTypes: true, readLabels: true})");
			
			writeSession.run(statement);
			
			return true;
			
		} catch (final Exception e) {return false;}
	}	
	
	/**
	 * Deletes the given backup file and changes the status to LOADING_BACKUP_FAILED
	 * @param backupFilePathOfInterest The backup file to delete
	 */
	private void deleteBackupAfterLoading(final String backupFilePathOfInterest) {
		if(executeSystemCommand(SUDO_REMOVE + backupFilePathOfInterest, LOADED_BACKUP)) {
			this.currentStatusCode = LOADING_BACKUP_FAILED;
			
			//Log status code
			logStatus();
			
			STARTER.startBackupFileNumberRevisorThread(LOADING_BACKUP_FAILED, this);						
		}
	}
}