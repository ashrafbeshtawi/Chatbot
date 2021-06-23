package de.dailab.oven.database.query;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.model.database.Operator;
import de.dailab.oven.model.database.NodeLabel;

/**
 * Class for storing and retrieving images
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class ImageQuery extends AQuery{
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(ImageQuery.class.getName());
	@Nonnull
	private static final String SEPARATOR = File.separator;
	@Nonnull
	private static final String ID_KEY = "id";
	@Nonnull
	private static final String IMAGE_DIRECTORY = "-images" + SEPARATOR;
	@Nonnull
	private static final String IMAGE_TYPE = ".png";
	@Nonnull
	private static final String IMAGE_PATH = "imagePath";
	@Nonnull
	private static final String AS = " " + Operator.AS.toDatabaseOperator() + " ";
	@Nonnull
	private static final String MATCH = Operator.MATCH.toDatabaseOperator();
	@Nonnull
	private static final String RETURN = Operator.RETURN.toDatabaseOperator();
	@Nonnull
	private static final String MATCH_ID = MATCH + "(n) WHERE ID(n) = $" + ID_KEY + " ";
	@Nonnull
	private static final String RETURN_IMAGE_PATH = RETURN + " n." + IMAGE_PATH + AS + IMAGE_PATH;
	
    @Nullable
    private String dataDirectory;
	@Nullable
	private final IdQuery idQuery;
	
	/**
	 * Initialize empty to set Graph later on
	 */
	public ImageQuery() {this(null);}
	
	/**
	 * Initialize ImageQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public ImageQuery(@Nullable Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
	
        try {
			this.dataDirectory = Configuration.getInstance().getProgramDataDirectory();
			if(!this.dataDirectory.endsWith(SEPARATOR))
				this.dataDirectory += SEPARATOR;
				
		} catch (ConfigurationException e) {
			this.dataDirectory = null;
			LOGGER.log(Level.WARNING, "Data directory is NULL. Querying is skipped");
		}
	}
	
	/**
	 * 
	 * @param image				The image to save
	 * @param targetNodeLabel	The node label which will hold the image reference
	 * @return					NULL in case passed file is NULL or directory is NULL / 
	 * 								could not be created.<br>
	 * 							The relative path where the image has been stored otherwise
	 * @throws IOException		In case storing failed with valid parameters
	 */
	public String saveImage(@Nullable RenderedImage image, @Nullable NodeLabel targetNodeLabel) throws IOException {

		//Just perform function if image is not null and do nothing if so
		if(image != null) {
			try {
				File imageFile = new File(this.dataDirectory + "tmpImage" 
						+ new File(this.dataDirectory).listFiles().length + ".png");
				
				ImageIO.write(image, "png", imageFile);
				
				String imagePath = saveImage(imageFile, targetNodeLabel);
				
				if(!Files.deleteIfExists(imageFile.toPath()))
					LOGGER.log(Level.INFO, "File {0} could not be deleted", imageFile.getAbsolutePath());
				
				return imagePath;				
			} catch(Exception e) {
				logNull(e.getLocalizedMessage());
				return null;
			}
		}
		else 
			logNull("Passed image is NULL");
		
		return null;
	}
	
	/**
	 * Stores the image and passes back the relative image path
	 * @param imageFile			The image file to store
	 * @param targetNodeLabel	The node label which will hold the image reference
	 * @return 					NULL in case passed file is NULL or directory is NULL / 
	 * 								could not be created.<br>
	 * 							The relative path where the image has been stored otherwise
	 * @throws IOException 		In case storing failed with valid parameters
	 */
	@Nullable
	public String saveImage(@Nullable File imageFile, @Nullable NodeLabel targetNodeLabel) throws IOException {
		
		//Even without the node label the image can be stored in undef-images/
		if(targetNodeLabel == null) 
			targetNodeLabel = NodeLabel.UNDEF;
		
		
		//Just perform function if image is not null and do nothing if so
		if(imageFile != null) {
			String directory = getDirectoryPath(targetNodeLabel);
			
			if(directory != null && !directory.isEmpty()) {			
				//Check if access was granted
				int numberOfFilesInDirectory = new File(directory).listFiles().length;
				if(numberOfFilesInDirectory != -1) {
					String imageReference = Integer.toString(numberOfFilesInDirectory + 1);
					String imageFileName = imageReference + IMAGE_TYPE;
					FileInputStream fI = new FileInputStream(imageFile);
					Path path = Paths.get(directory + imageFileName);
					Files.copy(fI, path);
					fI.close();
					//Set after writing to ensure imagePath stays "" if writing procedure crashes
					return targetNodeLabel.toString() + IMAGE_DIRECTORY + imageFileName;
				}
				else 
					logNull("Could not count files in directory: " + directory);					
			}
			else
				logNull("Directory is NULL or empty. Can not save image");				
		}
		else
			logNull("Passed image is NULL");			
		
		return null;
	}
	
	/**
	 * Returns the image or NULL
	 * @param nodeID			The node ID which holds the image reference
	 * @param expectedNodeLabel	The node label to expect with the given ID
	 * @return					The valid image<br>NULL in case image file does not exist,
	 * 								invalid request or lost database connection
	 * @throws IOException 		In case loading image with valid file failed
	 */
	@Nullable
	public RenderedImage loadImage(@Nullable Long nodeID, @Nullable NodeLabel expectedNodeLabel) {
		
		String imagePath = getImagePathFromDatabase(nodeID, expectedNodeLabel);
		
		if(imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists()) {
			try {
				File imageFile = new File(imagePath);
				return ImageIO.read(imageFile);
			} catch (Exception e) {
				logNull(e.getLocalizedMessage());
				return null;
			}
		}
		
		logLoadingImageFailed(nodeID, expectedNodeLabel);
		return null;
	}
	
	/**
	 * Returns the image file or NULL
	 * @param nodeID			The node ID which holds the image reference
	 * @param expectedNodeLabel	The node label to expect with the given ID
	 * @return					The valid image file<br>NULL in case file does not exist,
	 * 								invalid request or lost database connection
	 */
	@Nullable
	public File loadImageFile(long nodeID, @Nullable NodeLabel expectedNodeLabel) {
		String imagePath = getImagePathFromDatabase(nodeID, expectedNodeLabel);
		
		if(imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists()) {
			try {
				return new File(imagePath);							
			} catch (Exception e) {
				logNull(e.getLocalizedMessage());
			}
		}
		
		logLoadingImageFailed(nodeID, expectedNodeLabel);
		return null;
	}
	
	/**
	 * Tests if the given ID matches the node expected node type and extracts the image path
	 * @param nodeID			The node ID the image is requested for
	 * @param expectedNodeLabel	The node label to expect
	 * @return					NULL in case of ID and node label do not match, 
	 * 								data directory is NULL or lost database connection<br>
	 * 							Empty string if there is no image reference<br>
	 * 							The matching image path otherwise
	 */
	private String getImagePathFromDatabase(@Nullable Long nodeID, @Nullable NodeLabel expectedNodeLabel) {
		
		Boolean validID = this.idQuery.isNodeIdValid(nodeID, expectedNodeLabel);
		
		if(Boolean.TRUE.equals(validID) && this.dataDirectory != null) {
			
			try(Session readSession = this.graph.openReadSession()) {
				Statement statement = new Statement(MATCH_ID + RETURN_IMAGE_PATH);
				StatementResult result = readSession.run(statement.withParameters(
						Values.parameters(ID_KEY, nodeID)));
				
				if(result.hasNext()) {
					Record record = result.single();
					if(record.containsKey(IMAGE_PATH)) {
						String filePath = record.get(IMAGE_PATH).asString();
						if(!filePath.toLowerCase().contentEquals("null"))
							return this.dataDirectory + record.get(IMAGE_PATH).asString();													
					}
				}
				
				LOGGER.log(Level.INFO, "No image reference found for {0} with ID {1}",
						new Object[] {expectedNodeLabel, nodeID});
				
				return "";
			} catch (Exception e) {
				logLostConnection(LOGGER);
			}
		}
		
		logNull("Expected node label does not match ID or directory is NULL");
		return null;
	}
	
	/**
	 * Returns the matching directory and creates it if it is absent
	 * @param nodeLabel	The node label for which the image should be stored
	 * @return			The path as string<br>NULL in case of failure
	 */
	@Nullable
	private String getDirectoryPath(@Nonnull NodeLabel nodeLabel) {
		if(this.dataDirectory != null) {
			String directory = this.dataDirectory + nodeLabel.toString() + IMAGE_DIRECTORY;
			if(createDirectoryIfAbsent(directory)) 
				return directory;	
		}
		
		return null;
	}
	
	/**
	 * @param destinationPath	Path of directory to create if absent
	 * @return					True either if directory was created or existed; False if creation was not possible
	 */
	public boolean createDirectoryIfAbsent(final String destinationPath) {
		final File theDir = new File(destinationPath);
		boolean result = false;
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			//Split Path to check for each directory
			try {
				final String[] parts = destinationPath.split("\\\\");
				StringBuilder path = new StringBuilder();
				path.append("\\");
				//Test if parent directory exists
				for(int i = 0; i < parts.length; i++) {
					if(!parts[i].contentEquals("")) {
						path.append(parts[i]);
						path.append("\\");
						final File parentDir = new File(path.toString());
						if(!parentDir.exists()) {
							makeDir(parentDir);
						}
					}
				}
				result = true;
			}
			catch(final Exception e){
				LOGGER.log(Level.INFO, "Destination path is invalid", e.getCause());
			}
		}
		else {
			result = true;
		}
		return result;
	}

	/**
	 * Creates the given directory
	 * @param dir The directory to create
	 */
	private void makeDir(@Nonnull File dir) {
		try{
			dir.mkdir();
		}
		catch(final SecurityException se){
			LOGGER.log(Level.INFO, "Something went wrong while creating directory", se.getCause());
		}
	}
	
	/**
	 * Logs that loading image failed
	 * @param nodeID			The queried ID
	 * @param expectedNodeLabel	The expected node label
	 */
	private void logLoadingImageFailed(long nodeID, @Nullable NodeLabel expectedNodeLabel) {
		LOGGER.log(Level.INFO, "Loading image for {0} with ID {1} failed", new Object[] {expectedNodeLabel, nodeID});
	}
	
	/**
	 * Logs that a failure causes a NULL return
	 * @param message The message to add to the logging
	 */
	private void logNull(@Nonnull String message) {
		LOGGER.log(Level.INFO, "{0}. NULL is returned", Objects.requireNonNull(message));
	}
}
