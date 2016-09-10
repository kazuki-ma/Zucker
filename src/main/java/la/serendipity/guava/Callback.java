package la.serendipity.guava;

import java.util.concurrent.CompletableFuture;

import com.google.common.util.concurrent.FutureCallback;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(staticName = "fromJava")
public class Callback<T> implements FutureCallback<T> {
    @NonNull
    private final CompletableFuture<T> completableFuture;

    @Override
    public void onSuccess(T result) {
        completableFuture.complete(result);
    }

    @Override
    public void onFailure(Throwable t) {
        completableFuture.completeExceptionally(t);
    }
}
