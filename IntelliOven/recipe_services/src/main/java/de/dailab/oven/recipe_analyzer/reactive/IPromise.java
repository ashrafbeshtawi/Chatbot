package de.dailab.oven.recipe_analyzer.reactive;

/**
 * Callback used for asynchronous tasks that can either succeed or fail.
 *
 * @param <S> Data returned upon success
 * @param <E> Data returned upon error
 */
public interface IPromise<S, E>
{

    /**
     * Called on successful task completion.
     *
     * @param result The data resulting from the operation
     */
    void onSuccess(S result);

    /**
     * Called when task fails with an error
     *
     * @param reason Data describing the error
     */
    void onError(E reason);

}
