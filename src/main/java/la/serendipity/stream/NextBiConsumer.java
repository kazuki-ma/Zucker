package la.serendipity.stream;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
class NextBiConsumer<T> implements BiConsumer<Void, Throwable> {
    private static final String FUNCTION_NULL_RESULT_MESSAGE =
            "Function result is null. Should be CompletionStage";
    @NonNull
    final Iterator<T> iterator;
    @NonNull
    final Function<T, ? extends CompletionStage<Void>> function;
    @NonNull
    final CompletableFuture<Void> futureRoot;
    @NonNull
    final Executor executor;
    @NonNull
    final AtomicInteger successCount;
    @NonNull
    final AtomicInteger failedCount;

    @Override
    public void accept(Void b, Throwable t) {
        final T next;
        CompletionStage<Void> completionStage = null;

        try {
            synchronized (iterator) {
                if (!iterator.hasNext()) {
                    futureRoot.complete(null);
                    return;
                }
                next = iterator.next();
            }
            completionStage = function.apply(next);

            if (completionStage == null) {
                completionStage = failedFuture(new NullPointerException(FUNCTION_NULL_RESULT_MESSAGE));
            }
        } catch (Exception | Error e) {
            completionStage = failedFuture(e);
        }

        completionStage
                .whenComplete((val, throwable) -> {
                    if (throwable == null) {
                        successCount.incrementAndGet();
                    } else {
                        failedCount.incrementAndGet();
                    }
                })
                .whenCompleteAsync(this/* next */, executor);
    }

    static CompletableFuture<Void> failedFuture(@NonNull final Throwable throwable) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }
}
