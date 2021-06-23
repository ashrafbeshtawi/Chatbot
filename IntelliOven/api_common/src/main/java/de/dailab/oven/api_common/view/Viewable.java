package de.dailab.oven.api_common.view;

import de.dailab.oven.api_common.Sendable;

/**
 * This Interface provides functionality to show a Response in the View-API
 */
public interface Viewable extends Sendable {

    /**
     * Navigae up (Language or other input method)
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
	boolean up();

    /**
     * Navigae down (Language or other input method)
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
	boolean down();

    /**
     * Navigae left (Language or other input method)
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
	boolean left();

	/**
	 * Navigae right (Language or other input method)
	 *
	 * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
	 */
	boolean right();

	boolean volUp();

	boolean volDown();

	boolean mute();

	boolean action();

	boolean back();

	boolean forth();

    /**
     * Set Vieew to a certain object
     *
     * @param index index of the Object
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
	boolean set(int index);

}
