package thrift.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.async.AsyncMethodCallback;

/**
 * An implementation of {@link AsyncMethodCallback<T>} that allows chaining of
 * other implementations of {@link AsyncMethodCallback<T>}s.
 *
 * @author uday
 */
public class ListenableAsyncMethodCallback<T>
        implements AsyncMethodCallback<T> {

    private final List<AsyncMethodCallbackListener<T>> listeners
            = new ArrayList<>();

    /**
     * Adds a listener to completion/error events.
     *
     * @param listener
     *         a {@link AsyncMethodCallback<T>} that wants to be notified about
     *         the events.
     * @param executor
     *         an {@link Executor} on which the listener wants to be notified
     *         on.
     */
    public void addListener(AsyncMethodCallbackListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void onComplete(T response) {
        listeners.forEach((l) -> {
            try {
                l.getExecutor().execute(()
                        -> l.getAsyncMethodCallback().onComplete(response));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(Exception exception) {
        listeners.forEach((l) -> {
            try {
                l.getExecutor().execute(()
                        -> l.getAsyncMethodCallback().onError(exception));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
