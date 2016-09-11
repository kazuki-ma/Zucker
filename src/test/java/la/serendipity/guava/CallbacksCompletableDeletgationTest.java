package la.serendipity.guava;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

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
        future.complete("DONE");
        future.whenComplete(target);

        // Verify
        assertThat(future.isDone()).isTrue();
        assertThat(future.get()).isEqualTo("DONE");
    }

    @Test(expected = ExecutionException.class)
    public void exceptionallyTest() throws Exception {
        // Do
        future.completeExceptionally(new RuntimeException());
        future.whenComplete(target);

        // Verify
        assertThat(future.isDone()).isTrue();
        assertThat(future.isCompletedExceptionally()).isTrue();
        assertThat(future.get());
    }

    @Test(expected = NullPointerException.class)
    public void nullConstructorTest() {
        Callbacks.completableFuturePropagation(null);
    }
}
