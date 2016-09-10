package la.serendipity.closeable;

import java.util.concurrent.ExecutorService;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@AllArgsConstructor
class CloseableExecutorServiceImpl implements CloseableExecutorService {
    @Delegate(types = ExecutorService.class)
    private final ExecutorService delegate;

    @Override
    public void close() {
        delegate.shutdown();
    }
}
