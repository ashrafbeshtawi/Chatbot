package de.dailab.oven.database.parse;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ANeo4JParser {

	@Nonnull
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	@Nonnull
	protected static final ForkJoinPool POOL = ForkJoinPool.commonPool();	
	/**
	 * Waits for the latch to finish and re-interrupts the thread in case of failure
	 * @param countDownLatch The latch to wait for
	 */
	protected void waitForLatch(@Nonnull CountDownLatch countDownLatch) {
		Objects.requireNonNull(countDownLatch, "CountDownLatch must not be NULL");
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			log.log(Level.FINE, "Parsing got interrupted. Re-interrupting the thread", e);
			Thread.currentThread().interrupt();
		}					
	}
	
	/**
	 * Logs that parsing failed since of given exception
	 * @param thrownException The previously thrown exception
	 */
	protected void logParsingFailed(@Nullable Exception thrownException) {
		String message = "unkown reason";
		
		if(thrownException != null)
			message = thrownException.getLocalizedMessage();
		
		log.log(Level.INFO, "Could not parse ingredient since of {0}", message);
	}
}
