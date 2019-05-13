package thrift.interceptor;

import java.util.concurrent.Executor;

import org.apache.thrift.async.AsyncMethodCallback;


import thrift.interceptor.utils.DirectExecutor;

/**
 * Listener that wants to be notified about final result/exception set after
 * the actual handler finishes processing a Thrift request.
 *
 * @author uday
 */
public class AsyncMethodCallbackListener<T> {

    public static final AsyncMethodCallbackListener<Object> NO_OP_INSTANCE =
            new AsyncMethodCallbackListener<>(new AsyncMethodCallback<Object>() {
                @Override
                public void onComplete(Object response) {
                    // do nothing.
                }

                @Override
                public void onError(Exception exception) {
                    // do nothing.
                }
            }, DirectExecutor.INSTANCE);

    private final AsyncMethodCallback<T> asyncMethodCallback;
    private final Executor executor;

    public AsyncMethodCallbackListener(AsyncMethodCallback<T> asyncMethodCallback,
                                       Executor executor) {
        this.asyncMethodCallback = asyncMethodCallback;
        this.executor = executor;
    }

    public AsyncMethodCallback<T> getAsyncMethodCallback() {
        return asyncMethodCallback;
    }

    public Executor getExecutor() {
        return executor;
    }
}
