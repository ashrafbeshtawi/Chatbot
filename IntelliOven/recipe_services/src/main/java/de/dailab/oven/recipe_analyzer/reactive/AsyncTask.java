package de.dailab.oven.recipe_analyzer.reactive;

/**
 * Implements a promise, similar to JavaScript promises. Here's an introduction https://developers.google.com/web/fundamentals/primers/promises.
 *
 * At their most basic, promises are a bit like event listeners except:
 *  - A promise can only succeed or fail once. It cannot succeed or fail twice, neither can it switch from success to failure or vice versa.
 *  - If a promise has succeeded or failed and you later add a success/failure callback, the correct callback will be called, even though the event took place earlier.
 *
 * @param <T> The success type of the operation
 * @param <U> The error type of the operation
 */
public class AsyncTask<T, U> extends Observable<IPromise<T, U>>
{
    private T successData = null;
    private U errorData = null;

    @Override
    public void subscribe(final IPromise<T, U> handler)
    {
        if(this.successData != null)
        {
            handler.onSuccess(this.successData);
        }
        else if(this.errorData != null)
        {
            handler.onError(this.errorData);
        }
        else
        {
            super.subscribe(handler);
        }
    }

    public void sendSuccess(final T successData)
    {
        if(isPromiseFulfilled())
        {
            throw new IllegalStateException("This promise is fulfilled; promises can only succeed or fail once.");
        }

		this.successData = successData;
        sendEvent(e -> e.onSuccess(successData));
    }

    public void sendError(final U errorData)
    {
        if(isPromiseFulfilled())
        {
            throw new IllegalStateException("This promise is fulfilled; promises can only succeed or fail once.");
        }

		this.errorData = errorData;
        sendEvent(e -> e.onError(errorData));
    }

    private boolean isPromiseFulfilled()
    {
        return this.successData != null || this.errorData != null;
    }
}
