package thrift.interceptor.utils;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that executes the task on the same thread that invoked
 * {@link this#execute(Runnable)} method.
 *
 * @author uday
 */
public class DirectExecutor implements Executor {

    public static final DirectExecutor INSTANCE = new DirectExecutor();

    private DirectExecutor() {}

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
