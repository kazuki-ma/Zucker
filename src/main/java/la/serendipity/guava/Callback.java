package la.serendipity.guava;

import java.util.function.BiConsumer;

import com.google.common.util.concurrent.FutureCallback;

public interface Callback<T> extends BiConsumer<T, Throwable>, FutureCallback<T> {
    @Override
    default void accept(T result, Throwable throwable) {
        if (throwable == null) {
            onSuccess(result);
        } else {
            onFailure(throwable);
        }
    }
}
