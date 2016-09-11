package la.serendipity.closeable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;

import org.junit.Test;

public class CloseableExecutorServiceImplTest {
    @Test
    public void closeTest() throws Exception {
        CloseableExecutorService target = new CloseableExecutorServiceImpl(Executors.newFixedThreadPool(1));

        // Do
        target.close();

        // Verify
        assertThat(target.isShutdown()).isTrue();
    }
}
