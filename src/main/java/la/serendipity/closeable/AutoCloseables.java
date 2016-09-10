package la.serendipity.closeable;

import java.util.concurrent.ExecutorService;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AutoCloseables {
    public static CloseableExecutorService closeable(@NonNull final ExecutorService executorService) {
        return new CloseableExecutorServiceImpl(executorService);
    }
}
