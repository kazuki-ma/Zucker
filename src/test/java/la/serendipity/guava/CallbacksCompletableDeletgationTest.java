package la.serendipity.guava;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Futures;

public class CallbacksCompletableDeletgationTest {
    private CompletableFuture<String> future;
    private Callback<String> target;

    @Before
    public void setUp() throws Exception {
        future = new CompletableFuture<>();
        target = Callbacks.completableFuturePropagation(future);
    }

    @Test
    public void doneTest() throws Exception {
        // Do
        completedFuture("DONE").whenComplete(target);

        // Verify
        assertThat(future.isDone());
        assertThat(future.get()).isEqualTo("DONE");
    }

    @Test(expected = ExecutionException.class)
    public void exceptionallyTest() throws Exception {
        // Do
        Futures.addCallback(immediateFailedFuture(new RuntimeException()), target);

        // Verify
        assertThat(future.isDone());
        assertThat(future.get());
    }
}
