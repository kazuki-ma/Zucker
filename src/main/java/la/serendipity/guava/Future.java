package la.serendipity.guava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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
        public boolean cancel(boolean mayInterruptIfRunning);
    }

    public static <T> Future<T> fromGuava(@NonNull final ListenableFuture<T> listenableFuture) {
        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        Futures.addCallback(listenableFuture, Callback.<T>fromJava(completableFuture));

        return Future.fromJava(completableFuture);
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
