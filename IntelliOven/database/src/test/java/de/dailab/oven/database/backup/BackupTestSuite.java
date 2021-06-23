package de.dailab.oven.database.backup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	BackupConfigurationTest.class,
	BackupEventsTest.class,
	CriticalDatabaseStateListenerTest.class,
	CriticalStateObserverTest.class,
	DatabaseListenerTest.class,
	ThreadInitializationTest.class,
	ThreadStarterTest.class
})

public class BackupTestSuite {}
