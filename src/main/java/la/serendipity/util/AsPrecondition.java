package la.serendipity.util;

public class AsPrecondition {
    public static class PreconditionException extends IllegalArgumentException {
        public PreconditionException(String s) {
            super(s);
        }
    }

    public static void shouldBe(final boolean assertion, final Object message) {
        if (!assertion) {
            throw new PreconditionException(String.valueOf(message));
        }
    }

    public static void rejectIf(final boolean assertion, final Object message) {
        if (assertion) {
            throw new PreconditionException(String.valueOf(message));
        }
    }
}
