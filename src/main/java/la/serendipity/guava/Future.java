package la.serendipity.guava;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;

@AllArgsConstructor(staticName = "fromJava")
public class Future<T> extends CompletableFuture<T> implements ListenableFuture<T> {
    @Delegate
    @NonNull
    private final CompletableFuture<T> delegate;

    public static <T> Future<T> fromGuava(@NonNull final ListenableFuture<T> listenableFuture) {
        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        Futures.addCallback(listenableFuture, Callback.<T>fromJava(completableFuture));

        return Future.fromJava(completableFuture);
    }

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        delegate.thenRunAsync(listener, executor);
    }
}
