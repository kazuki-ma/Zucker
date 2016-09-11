package la.serendipity.guava;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.testing.AbstractListenableFutureTest;

import lombok.SneakyThrows;

public class ExtendedFutureTest extends AbstractListenableFutureTest {

    @Override
    protected <V> ListenableFuture<V> createListenableFuture(V value, Exception except, CountDownLatch waitOn) {
        final CompletableFuture<V> delegate = new CompletableFuture<>();
        final Future<V> vFuture = Future.fromJava(delegate);

        new Thread() {
            @Override
            @SneakyThrows
            public void run() {
                waitOn.await();
                delegate.complete(value);
            }
        }.start();

        return vFuture;
    }
}
