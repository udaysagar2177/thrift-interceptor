package thrift.interceptor.example.server;

import java.lang.reflect.Method;

import org.apache.thrift.TException;


import thrift.interceptor.AsyncMethodCallbackListener;
import thrift.interceptor.RequestInterceptor;
import thrift.interceptor.example.multiply.Options;

/**
 * Intercepts a Thrift request to determine if the client has disconnected and abandoned the
 * request.
 *
 * @author uday
 */
public class RequestTimeoutInterceptor implements RequestInterceptor {

    public static final TException REQUEST_TIMEOUT_EXCEPTION = new
            TException("Client timed out");

    /**
     * Determines if a thrift request is abandoned by client based on the
     * timeout supplied in the {@link MoreOptions}.
     *
     * @param method
     *         Method that would be invoked.
     * @param args
     *         arguments that would be passed to method.
     *
     * @return {@link AsyncMethodCallbackListener.NO_OP_INSTANCE} as this
     *         interceptor is not interested in final result.
     * @throws TException
     *         if the thrift request is deemed as abandoned by client.
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
            if (options.isSetTimeout()
                    && System.currentTimeMillis() >= options.getTimeout()) {
                throw REQUEST_TIMEOUT_EXCEPTION;
            }
            break;
        }
        return AsyncMethodCallbackListener.NO_OP_INSTANCE;
    }
}
