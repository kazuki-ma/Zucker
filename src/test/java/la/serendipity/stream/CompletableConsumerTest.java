package la.serendipity.stream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static la.serendipity.closeable.AutoCloseables.closeable;
import static la.serendipity.guava.Future.failedFuture;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import la.serendipity.closeable.CloseableExecutorService;

public class CompletableConsumerTest {
    Stream<Integer> stream;
    CloseableExecutorService executorService;

    @Before
    public void setUp() {
        stream = IntStream.rangeClosed(1, 100).boxed();
        executorService = closeable(newFixedThreadPool(3));
    }

    @After
    public void tearDown() {
        stream.close();
        executorService.shutdown();
    }

    @Test
    public void testWithThreadPoolExecutor() throws ExecutionException, InterruptedException {

        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(3, executorService);
        AtomicInteger ai = new AtomicInteger();

        // Do
        target.apply(stream, i -> {
            ai.addAndGet(i);
            return completedFuture(null);
        }).get();

        // Verify
        assertThat(ai.get())
                .isEqualTo(100 * (100 + 1) / 2);
        assertThat(target.getSuccessCount())
                .isEqualTo(100);
        assertThat(target.getFailedCount())
                .isEqualTo(0);
        assertThat(target.getExecutedCount())
                .isEqualTo(100);
    }

    @Test
    public void testWithFailed() throws ExecutionException, InterruptedException {
        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(3, executorService);
        AtomicInteger ai = new AtomicInteger();

        // Do
        target.apply(stream, i -> {
            ai.addAndGet(i);

            switch (i % 3) {
                case 0:
                    return completedFuture(null);
                case 1:
                    throw new RuntimeException();
            }
            return failedFuture(new RuntimeException());
        }).get();

        // Verify
        assertThat(ai.get())
                .isEqualTo(100 * (100 + 1) / 2);
        assertThat(target.getSuccessCount())
                .isEqualTo(33);
        assertThat(target.getFailedCount())
                .isEqualTo(67);
        assertThat(target.getExecutedCount())
                .isEqualTo(100);
    }

    @Test
    public void testStackerIsNotOccurredWithTooLongStream() throws ExecutionException, InterruptedException {
        stream = IntStream.rangeClosed(1, 1_000_000).boxed();
        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(3, executorService);

        target.apply(stream, (i) -> completedFuture(null)).get();

        assertThat(target.getSuccessCount())
                .isEqualTo(1_000_000);
    }

    @Test
    public void emptyResponseOnFunctionTreatedTest() throws ExecutionException, InterruptedException {
        stream = IntStream.rangeClosed(1, 1).boxed();
        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(3, executorService);

        target.apply(stream, (i) -> null).get();

        assertThat(target.getFailedCount())
                .isEqualTo(1);
    }

    @Test
    public void exceptionOnFunctionTreatedTest() throws ExecutionException, InterruptedException {
        stream = IntStream.rangeClosed(1, 1).boxed();
        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(3, executorService);

        target.apply(stream, (i) -> {
            throw new RuntimeException();
        }).get();

        assertThat(target.getFailedCount())
                .isEqualTo(1);
    }

    @Test
    public void tooManyStreamTest() throws Exception {
        stream = IntStream.rangeClosed(1, 10_000).boxed();
        ParallelStreamConsumer<Integer> target = new ParallelStreamConsumer<>(1024,
                                                                              Executors.newFixedThreadPool(16));

        final AtomicLong atomicLong = new AtomicLong();
        final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(8);

        target.apply(stream, (Integer i) -> {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            scheduledThreadPoolExecutor.schedule(() -> {
                atomicLong.addAndGet((long) i);
                completableFuture.complete(null);
            }, 100, TimeUnit.MILLISECONDS);
            return completableFuture;
        }).get();

        // Verify
        assertThat(atomicLong.get())
                .isEqualTo(10_000L * (1L + 10_000L) / 2L);
    }

}