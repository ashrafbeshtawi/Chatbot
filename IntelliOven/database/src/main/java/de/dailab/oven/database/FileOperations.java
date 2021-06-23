package de.dailab.oven.database;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Paths;

public class FileOperations {

	private FileOperations() {}

	/**
	 * Returns boolean if a file exists with given filePath
	 * @param filePath	FilePath of file to check
	 * @return true, if file exists; false if not or null
	 */
	public static boolean fileExists(@Nullable final String filePath) {
		if(filePath != null) {
			return Paths.get(filePath).toFile().exists();
		}
		return false;
	}

	/**
	 * 
	 * @param filePath
	 * @return -1 in case of input string equals null; ~0 in case file did not exist beforehand; every other output is valid in case system time is correct
	 */
	public static long minutesSinceLastModified(@Nullable final String filePath) {
		if(filePath != null) {
			final File file = new File(filePath);
			return (System.currentTimeMillis() - file.lastModified())/60000;
		}
		return -1;
	}
	
}
