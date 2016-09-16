package la.serendipity.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import la.serendipity.closeable.SilentClose;
import la.serendipity.util.AsPrecondition;
import lombok.NonNull;
import lombok.SneakyThrows;

public class ParallelStreamConsumer<T>
        implements
        BiFunction<
                Stream<T>,
                Function<T, ? extends CompletionStage<Void>>,
                CompletableFuture<Void>> {
    private static final Void VOID = null;
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();

    private final int parallelCount;
    private final Executor executor;

    public ParallelStreamConsumer(
            final int parallelCount,
            @NonNull final Executor executor) {
        AsPrecondition.shouldBe(parallelCount >= 1, "parallelCount should be >= 1");

        this.parallelCount = parallelCount;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Void> apply(
            @NonNull final Stream<T> stream,
            @NonNull final Function<T, ? extends CompletionStage<Void>> function) {

        final Semaphore available = new Semaphore(parallelCount);
        final CompletableFuture<Void> futureRoot = new CompletableFuture<>();

        try (SilentClose silent = stream::close) {
            stream.onClose(() -> {
                uncheckedAcquire(available, parallelCount);
                futureRoot.complete(null);
            }).forEach(value -> {
                uncheckedAcquire(available, 1);

                executor.execute(() -> {
                    CompletionStage<?> completionStage;

                    try {
                        completionStage = function.apply(value);

                        if (completionStage == null) {
                            throw new NullPointerException();
                        }
                    } catch (Exception | Error e) {
                        CompletableFuture<?> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        completionStage = future;
                    }

                    completionStage
                            .whenComplete((v, e) -> {
                                if (e == null) {
                                    successCount.incrementAndGet();
                                } else {
                                    failedCount.incrementAndGet();
                                }

                                available.release();
                            });
                });
            });

            return futureRoot;
        }
    }

    @SneakyThrows
    static void uncheckedAcquire(Semaphore semaphore, int permit) {
        semaphore.acquire(permit);
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public int getExecutedCount() {
        return successCount.get() + failedCount.get();
    }
}
