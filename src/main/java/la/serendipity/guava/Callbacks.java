package la.serendipity.guava;

import java.util.concurrent.CompletableFuture;

import com.google.common.util.concurrent.SettableFuture;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Callbacks {
    public static <T> Callback<T> settableFuturePropagation(SettableFuture<T> settableFuture) {
        return new SettableFuturePropagator<>(settableFuture);
    }

    public static <T> Callback<T> completableFuturePropagation(CompletableFuture<T> future) {
        return new CompletableFuturePropagator<>(future);
    }

    @AllArgsConstructor
    static class SettableFuturePropagator<T> implements Callback<T> {
        @NonNull
        private final SettableFuture<T> settableFuture;

        @Override
        public void onSuccess(T result) {
            settableFuture.set(result);
        }

        @Override
        public void onFailure(Throwable t) {
            settableFuture.setException(t);
        }
    }

    @AllArgsConstructor(staticName = "fromJava")
    static class CompletableFuturePropagator<T> implements Callback<T> {
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
}
