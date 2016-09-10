package la.serendipity.closeable;

import java.util.concurrent.ExecutorService;

public interface CloseableExecutorService extends ExecutorService, AutoCloseable {
    /**
     * call {@code ExecutorService#shutdown()}
     *
     * @see ExecutorService#shutdown()
     */
    @Override
    void close();
}
