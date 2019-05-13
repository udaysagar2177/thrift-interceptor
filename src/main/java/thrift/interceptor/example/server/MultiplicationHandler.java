package thrift.interceptor.example.server;

import org.apache.thrift.async.AsyncMethodCallback;


import thrift.interceptor.example.multiply.MultiplicationService;
import thrift.interceptor.example.multiply.Options;

/**
 * An implementation of {@link MultiplicationService.AsyncIface}.
 *
 * @author uday
 */
public class MultiplicationHandler implements MultiplicationService.AsyncIface {

    @Override
    public void multiply(int n1, int n2, Options options,
                         AsyncMethodCallback<Integer> resultHandler) {
        System.out.println("Multiply(" + n1 + "," + n2 + ")");
        try {
            int result = n1 * n2;
            resultHandler.onComplete(result);
        } catch (Exception e) {
            resultHandler.onError(e);
        }
    }
}
