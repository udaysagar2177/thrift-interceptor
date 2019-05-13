package thrift.interceptor.example.client;

import java.net.SocketTimeoutException;
import java.util.stream.IntStream;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


import thrift.interceptor.example.multiply.MultiplicationService;
import thrift.interceptor.example.multiply.Options;

/**
 * A simple Thrift client implementation that calls a multiplication endpoint on
 * {@link MultiplicationService}.
 *
 * On each request, a timeout value is added which denotes the time in epoch ms at which the client
 * will abandon waiting for the results. In addition to the timeout, a client id is passed in the
 * request as well which would help the server decide the priority/rejection of a request.
 *
 * @author uday
 */
public class Client {
    public static final int SOCKET_TIMEOUT = 1000;

    public static void main(String[] args) {
        Client client = new Client();
        IntStream.range(0, 5).parallel().forEach(client::makeCallsToServer);
    }

    protected void makeCallsToServer(int clientId) {
        try {
            TSocket tSocket = new TSocket("localhost", 9090);
            tSocket.setTimeout(60000); // client is going to wait for 60s.
            tSocket.open();
            TTransport transport;
            transport = new TFramedTransport(tSocket);

            TProtocol protocol = new TBinaryProtocol(transport);
            MultiplicationService.Client client = new MultiplicationService
                    .Client(protocol);
            perform(client, clientId);
            tSocket.close();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    protected void perform(MultiplicationService.Client client, int clientId) {
        try {
            int a = 3, b = 5;
            int product = multiply(client, clientId, a, b);
            System.out.println(String.format("%s * %s = %s", a, b, product));
        } catch (Exception e) {
            if (e instanceof TTransportException && e
                    .getCause() instanceof SocketTimeoutException) {
                System.out.println("Server didn't respond within timeout");
                return;
            }
            e.printStackTrace();
        }
    }

    protected int multiply(MultiplicationService.Iface client, int clientId, int a,
                           int b) throws Exception {
        return client.multiply(a, b,
                new Options(System.currentTimeMillis() + SOCKET_TIMEOUT, clientId));
    }
}

