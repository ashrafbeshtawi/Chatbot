package de.dailab.oven.database.validate;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AValidator {

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(AValidator.class.getName());
	@Nonnull
	private final Set<AValidator> validators = new HashSet<>();
	
	public abstract <T> boolean isValid(T t);
	
	/**
	 * Add a query to the set of queries
	 * @param validator	The {@link AValidator to add}
	 */
	public void addValidator(AValidator validator) {this.validators.add(validator);}
	
	/**
	 * @return The set of {@link AValidator}s
	 */
	public Set<AValidator> getValidators() {return this.validators;}
	
	/**
	 * Instantiates the class if.
	 * Adds it if necessary to the set of used validators.
	 * @param validatorClass The {@link AValidator}-subclass of interest
	 * @return A new instance or the already used instance
	 */
	@Nullable
	public AValidator getValidator(Class<?> validatorClass) {
				
		if(!validatorClass.getSuperclass().equals(AValidator.class)) return null;
				
		for(AValidator v : this.validators) {
			if(v.getClass().equals(validatorClass)) return v;
		}
		
		try {
			Object object = validatorClass.newInstance();
			AValidator aValidator = (AValidator) object;
			this.validators.add(aValidator);
			return aValidator;
		} catch (InstantiationException | IllegalAccessException e) {
			LOGGER.log(Level.INFO, "Could not instantiate {0}", validatorClass.getName());
			return null;
		}
	}
	
	/**
	 * Checks if the given object is an instance of the expected class
	 * @param <T>				The generic class
	 * @param object			The instance of the class
	 * @param expectedClass		The expected class
	 * @param logger			The logger to use
	 * @return					<tt>True</tt> if object is an instance of expected class<br>
	 * 							<tt>False</tt> otherwise
	 */
	protected <T> boolean isCorrectObject(@Nonnull T object, @Nonnull Class<?> expectedClass, 
			@Nonnull Logger logger) {
		
		if(!object.getClass().equals(expectedClass)){
			logInvalidClass(logger, expectedClass.getSimpleName(), 
					object.getClass().getSimpleName());
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Logs that the given parameter is NULL, which is not allowed
	 * @param message	The message (parameter) which is NULL
	 */
	protected void logNull(@Nonnull Logger logger, @Nullable String message) {
		if(message == null || message.isEmpty())
			message = "Unknown";
		
		logger.log(Level.INFO, "{0} must not be NULL", message);
	}
	
	/**
	 * Logs that the given parameter is invalid
	 * @param logger			The {@link Logger} to use
	 * @param parameterKey		The parameters key
	 * @param parameterValue	The current parameter value
	 * @param defaultValue		The default value to log for the current value
	 * @param className			The class name for a clear log
	 */
	protected void logInvalid(@Nonnull Logger logger, @Nullable String parameterKey, 
			@Nullable String parameterValue, @Nonnull String defaultValue, 
			@Nonnull String className) {
		
		if(parameterKey == null || parameterKey.isEmpty()) 
			parameterKey = "Unknown parameter";
		
		if(parameterValue == null || parameterValue.isEmpty()) 
			parameterValue = defaultValue;
		
		logger.log(Level.INFO, "{0} is invalid. Hence {1} {2} is invalid as well", 
				new Object[] {parameterKey, className, parameterValue});
	}
	
	/**
	 * Logs that the given class is not usable for validating
	 * @param logger			The logger to use
	 * @param expectedClass		The expected class
	 * @param actualClass		The actual retrieved class
	 */
	protected void logInvalidClass(@Nonnull Logger logger, @Nonnull String expectedClass, 
			@Nonnull String actualClass) {
		
		logger.log(Level.INFO, "Can not invoke validator for {0} on {1}", 
				new Object[] {actualClass, expectedClass});
	}
}