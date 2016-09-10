package la.serendipity.closeable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class SilentCloseTest {
    @Test
    public void closeNormallyTest() throws Exception {
        final AtomicInteger i = new AtomicInteger();

        try (SilentClose t = i::incrementAndGet) {
        }

        assertThat(i.get())
                .isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void closeExceptionallyTest() throws Exception {
        try (SilentClose t = () -> {
            throw new RuntimeException();
        }) {
        }
    }
}
