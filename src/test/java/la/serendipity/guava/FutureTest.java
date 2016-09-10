package la.serendipity.guava;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.util.concurrent.SettableFuture;

public class FutureTest {
    @Test
    public void fromGuavaResultValueByGuavaTest() {
        final SettableFuture<Integer> guavaFuture = SettableFuture.create();
        Future<Integer> integerFuture = Future.fromGuava(guavaFuture);

        // Do
        guavaFuture.set(1);

        // Verify
        assertThat(integerFuture.getNow(0)).isEqualTo(1);
    }

    @Test(expected = Exception.class)
    public void fromGuavaResultValueByJavaTest() throws Exception {
        final SettableFuture<Integer> guavaFuture = SettableFuture.create();
        Future<Integer> integerFuture = Future.fromGuava(guavaFuture);

        // Do
        integerFuture.complete(1);

        // Verify
        assertThat(guavaFuture.get(0, TimeUnit.SECONDS)).isEqualTo(1);
    }

    @Test
    public void fromGuavaCallbackTest() {
        final SettableFuture<Integer> guavaSourceFuture = SettableFuture.create();
        final Future<Integer> future = Future.fromGuava(guavaSourceFuture);

        final AtomicInteger result = new AtomicInteger(0);

        future.whenComplete((value, throwable) -> {
            result.set(value);
        });

        // Do
        guavaSourceFuture.set(1);

        // Verify
        assertThat(future.isDone()).isTrue();
        assertThat(result.get()).isEqualTo(1);
    }
}