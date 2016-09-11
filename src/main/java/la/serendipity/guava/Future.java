package la.serendipity.guava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import la.serendipity.guava.Callbacks.CompletableFuturePropagator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;

@AllArgsConstructor(staticName = "fromJava")
public class Future<T> extends CompletableFuture<T> implements ListenableFuture<T> {
    @Delegate(excludes = excludes.class)
    @NonNull
    private final CompletableFuture<T> delegate;

    private final List<Runnable> cancelListener = new ArrayList<>();

    private interface excludes {
        boolean cancel(boolean mayInterruptIfRunning);
    }

    public static <T> Future<T> fromGuava(@NonNull final SettableFuture<T> settableFuture) {
        return fromGuava(settableFuture, Callbacks.settableFuturePropagation(settableFuture));
    }

    public static <T> Future<T> fromGuava(
            @NonNull final ListenableFuture<T> listenableFuture,
            @NonNull final BiConsumer<T, Throwable> onComplete) {
        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        // When ListenableFuture complete, notify to CompletableFuture
        Futures.addCallback(listenableFuture, CompletableFuturePropagator.<T>fromJava(completableFuture));

        // When CompletableFuture complete, notify to ListenableFuture
        final Future<T> future = Future.fromJava(completableFuture);
        future.whenComplete(onComplete);

        return future;
    }

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        synchronized (this) {
            if (this.isCancelled()) {
                executor.execute(listener);
            } else {
                cancelListener.add(listener);
            }
        }

        delegate.thenRunAsync(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        final List<Runnable> cancelListenerCopy;
        synchronized (this) {
            cancelListenerCopy = new ArrayList<>(cancelListener);
            cancelListener.clear();
        }
        cancelListenerCopy.forEach(Runnable::run);
        return delegate.cancel(mayInterruptIfRunning);
    }

}
