package thrift.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TProtocol;


import thrift.interceptor.utils.DirectExecutor;

/**
 * A Middleware mechanism that allows interception of each Thrift request and take any
 * actions before/after processing the request.
 *
 * This class works just with asynchronous interface of a Thrift service. Only asynchronous
 * interface allows us to read the method arguments, set result or throw exception easily
 * without messing with input or output {@link TProtocol}s.
 *
 * @author uday
 */
public class ThriftMiddlewareProxy<T> implements InvocationHandler {

    private final T asyncIface;
    private final ExecutorService executorService;
    private final List<RequestInterceptor> requestCreationInterceptors;
    private final List<RequestInterceptor> requestPreProcessInterceptors;

    private ThriftMiddlewareProxy(T asyncIface, ExecutorService executorService,
                                  List<RequestInterceptor> requestCreationInterceptors,
                                  List<RequestInterceptor> requestPreProcessInterceptors) {
        this.asyncIface = asyncIface;
        this.executorService = executorService;
        this.requestCreationInterceptors = requestCreationInterceptors;
        this.requestPreProcessInterceptors = requestPreProcessInterceptors;
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<T> asyncIfaceClass, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(asyncIfaceClass.getClassLoader(),
                new Class<?>[] { asyncIfaceClass }, handler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        // update the AsyncMethodCallback in the args with
        // ListenableAsyncMethodCallback instance.
        ListenableAsyncMethodCallback<Object> listenableResultCallback =
                listenOnResultCallback(args);
        args[args.length - 1] = listenableResultCallback;

        processInterceptors(requestCreationInterceptors, method, args,
                listenableResultCallback);
        executorService.submit(() -> {
            try {
                processInterceptors(requestPreProcessInterceptors, method, args,
                        listenableResultCallback);
            } catch (Exception e) {
                listenableResultCallback.onError(e);
                return;
            }
            try {
                method.invoke(asyncIface, args);
            } catch (Exception e) {
                listenableResultCallback.onError(e);
            }
        });
        return null;
    }

    private ListenableAsyncMethodCallback<Object> listenOnResultCallback(Object[] args) {
        // thrift puts the AsyncMethodCallback argument at the end of each AsyncIface method.
        @SuppressWarnings("unchecked")
        AsyncMethodCallback<Object> resultCallback =
                (AsyncMethodCallback<Object>) args[args.length - 1];
        ListenableAsyncMethodCallback<Object> listenableResultCallback =
                new ListenableAsyncMethodCallback<>();
        listenableResultCallback
                .addListener(new AsyncMethodCallbackListener<>(resultCallback,
                        DirectExecutor.INSTANCE));
        return listenableResultCallback;
    }

    protected void processInterceptors(List<RequestInterceptor> interceptors, Method method,
                                       Object[] args,
                                       ListenableAsyncMethodCallback<Object> resultCallback)
            throws TException {
        for (RequestInterceptor interceptor : interceptors) {
            resultCallback.addListener(interceptor.intercept(method, args));
        }
    }

    public static class Builder<T> {

        private final List<RequestInterceptor> requestCreationInterceptors = new ArrayList<>();
        private final List<RequestInterceptor> requestPreProcessInterceptors = new ArrayList<>();

        private final T asyncIface;
        private final ExecutorService executorService;

        public Builder(T asyncIface, ExecutorService executorService) {
            this.asyncIface = asyncIface;
            this.executorService = executorService;
        }

        public Builder<T> addRequestCreationInterceptor(RequestInterceptor interceptor) {
            requestCreationInterceptors.add(interceptor);
            return this;
        }

        public Builder<T> addRequestPreProcessInterceptor(RequestInterceptor interceptor) {
            requestPreProcessInterceptors.add(interceptor);
            return this;
        }

        public ThriftMiddlewareProxy<T> build() {
            return new ThriftMiddlewareProxy<>(asyncIface, executorService,
                    Collections.unmodifiableList(requestCreationInterceptors),
                    Collections.unmodifiableList(requestPreProcessInterceptors));
        }
    }
}
