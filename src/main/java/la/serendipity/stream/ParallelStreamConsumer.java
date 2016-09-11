package la.serendipity.stream;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import la.serendipity.util.AsPrecondition;
import lombok.NonNull;

public class ParallelStreamConsumer<T>
        implements BiFunction<Stream<T>, Function<T, ? extends CompletionStage<Void>>,
        CompletableFuture<Void>> {
    private static final Void VOID = null;
    final AtomicInteger successCount = new AtomicInteger();
    final AtomicInteger failedCount = new AtomicInteger();

    final int parallelCount;
    final Executor executor;

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
        return apply(stream.iterator(), function);
    }

    public CompletableFuture<Void> apply(
            @NonNull final Iterator<T> iterator,
            @NonNull final Function<T, ? extends CompletionStage<Void>> function) {

        final CompletableFuture[] futures = new CompletableFuture[parallelCount];
        for (int i = 0; i < parallelCount; ++i) {
            final CompletableFuture<Void> futureRoot = new CompletableFuture<>();
            final NextBiConsumer<T> biConsumer =
                    new NextBiConsumer<>(iterator, function, futureRoot, executor, successCount, failedCount);

            completedFuture(VOID).whenCompleteAsync(biConsumer, executor);

            futures[i] = futureRoot;
        }
        return allOf(futures);
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
