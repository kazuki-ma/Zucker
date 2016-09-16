package la.serendipity.closeable;

@FunctionalInterface
public interface SilentClose extends AutoCloseable {
    @Override
    default void close() {
        try {
            closeInternal();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in #close()", e);
        }
    }

    void closeInternal() throws Exception;
}
