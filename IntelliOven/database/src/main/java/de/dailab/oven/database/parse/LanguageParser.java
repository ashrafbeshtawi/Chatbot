package de.dailab.oven.database.parse;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

/**
 * Class for parsing languages from database nodes
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class LanguageParser {

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(LanguageParser.class.getName());
	@Nonnull
	private static final String NAME_KEY = "name";
	@Nonnull
	private static final Language UNDEF = Language.UNDEF;
	
	/**
	 * Parses the language from the given node
	 * @param languageNode The node to parse
	 * @return 	Language.UNDEF if node is NULL or language is unknown<br>
	 * 			NULL if node is not a language node<br>
	 * 			The parsed language otherwise
	 */
	@Nullable
	public Language parseLanguageFromNode(@Nullable Node languageNode) {
		
		if(languageNode == null) {
			LOGGER.log(Level.INFO, "Node is NULL. Language.UNDEF is returned");
			return UNDEF;			
		}
		
		else if(!languageNode.hasLabel(NodeLabel.LANGUAGE.toDatabaseLabel())) {
			LOGGER.log(Level.INFO, "Node is not a language node. NULL is returned");
			return null;
		}
		
		else
			return Language.getLanguage(languageNode.get(NAME_KEY).asString());					
	}
}
