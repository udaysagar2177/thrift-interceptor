package thrift.interceptor.example.server;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;


import thrift.interceptor.ThriftMiddlewareProxy;
import thrift.interceptor.example.multiply.MultiplicationService;

/**
 * A Thrift server serving requests using {@link MultiplicationHandler}.
 *
 * This server uses {@link ThriftMiddlewareProxy<MultiplicationService.AsyncIface>} with two
 * interceptors - {@link RequestPermitInterceptor} and {@link RequestTimeoutInterceptor}
 * that allows it handle client requests efficiently.
 *
 * @author uday
 */
public class Server {

    public static void main(String[] args) {
        new Server().runServer();
    }

    protected MultiplicationService.AsyncIface getHandler() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10,
                TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        InvocationHandler handler = new ThriftMiddlewareProxy
                .Builder<>(new MultiplicationHandler(), executor)
                .addRequestCreationInterceptor(new RequestPermitInterceptor())
                .addRequestPreProcessInterceptor(new RequestTimeoutInterceptor())
                .build();
        return ThriftMiddlewareProxy
                .wrap(MultiplicationService.AsyncIface.class, handler);
    }

    protected void runServer() {
        MultiplicationService.AsyncIface handler = getHandler();
        MultiplicationService.AsyncProcessor<MultiplicationService.AsyncIface> processor =
                new MultiplicationService.AsyncProcessor<>(handler);
        Runnable serverRunnable = () -> createServerRunnable(processor);
        new Thread(serverRunnable).start();
    }

    protected void createServerRunnable(
            MultiplicationService.AsyncProcessor<MultiplicationService.AsyncIface> processor) {
        try {
            TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9090);
            TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport)
                    .transportFactory(new TFramedTransport.Factory())
                    .protocolFactory(new TBinaryProtocol.Factory())
                    .processor(processor);
            TServer server = new TNonblockingServer(args);

            System.out.println("Starting the simple server...");
            server.serve();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
