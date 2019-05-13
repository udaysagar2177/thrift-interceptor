package thrift.interceptor;

import java.lang.reflect.Method;

import org.apache.thrift.TException;

/**
 * Interface for intercepting Thrift requests.
 *
 * Note: Based on the current design, the interceptors shouldn't be blocking
 * because the thread on which the call is being intercepted is a non-blocking
 * thread.
 *
 * @author uday
 */
public interface RequestInterceptor {

    /**
     * Intercept a Thrift request.
     *
     * @param method
     *         Method that would be invoked to process actual thrift request.
     * @param args
     *         arguments that would be passed to method.
     *
     * @return a non-null {@link AsyncMethodCallbackListener<Object>} object
     *         that wants to be notified about results/exception set after
     *         processing the Thrift request. This method can also return
     *         {@link AsyncMethodCallbackListener#NO_OP_INSTANCE} if the
     *         interceptor doesn't care about final result/exception.
     */
    AsyncMethodCallbackListener<Object> intercept(Method method, Object[] args)
            throws TException;
}
