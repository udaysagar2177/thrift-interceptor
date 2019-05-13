package thrift.interceptor.example.server;

import java.lang.reflect.Method;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;


import thrift.interceptor.AsyncMethodCallbackListener;
import thrift.interceptor.RequestInterceptor;
import thrift.interceptor.example.multiply.Options;
import thrift.interceptor.utils.DirectExecutor;

/**
 * Intercepts a Thrift request to determine if it is allowed for execution.
 *
 * @author uday
 */
public class RequestPermitInterceptor implements RequestInterceptor {

    private static final TException REQUEST_PERMIT_REJECTED = new
            TException("Too many requests.");
    private static final int BAD_CLIENT_ID = 1;

    /**
     * Acquires permit that allows request execution.
     *
     * In this example, this method is going to reject any request from
     * clientId = {@link this#BAD_CLIENT_ID}.
     *
     * @param method
     *         Method that would be invoked.
     * @param args
     *         arguments that would be passed to method.
     *
     * @return a {@link AsyncMethodCallbackListener<Object>} that allows
     *         intercepting results/exception set after invoking the method.
     * @throws TException
     *         if a permit cannot be acquired.
     */
    @Override
    public AsyncMethodCallbackListener<Object> intercept(Method method,
                                                         Object[] args)
            throws TException {
        for (Object arg : args) {
            if (!(arg instanceof Options)) {
                continue;
            }
            Options options = (Options) arg;
            if (options.isSetClientId()
                    && options.getClientId() == BAD_CLIENT_ID) {
                throw REQUEST_PERMIT_REJECTED;
            }
            return new AsyncMethodCallbackListener<>(new AsyncMethodCallback<Object>() {
                @Override
                public void onComplete(Object response) {
                    // record success (For ex: increment permits).
                }

                @Override
                public void onError(Exception exception) {
                    // record exception (For ex: decrement permits)
                    // or ignore an expected error.
                }
            }, DirectExecutor.INSTANCE);
        }
        return AsyncMethodCallbackListener.NO_OP_INSTANCE;
    }
}
