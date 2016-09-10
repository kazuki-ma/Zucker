package la.serendipity.util;

import org.junit.Test;

public class AsPreconditionTest {
    static final Object MESSAGE_NOT_TO_BE = new Object() {
        @Override
        public String toString() {
            throw new RuntimeException("Never thrown");
        }
    };

    @Test
    public void shouldBePositiveTest() {
        AsPrecondition.shouldBe(true, MESSAGE_NOT_TO_BE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBeNegativeTest() {
        AsPrecondition.shouldBe(false, "TEST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectIfPositiveTest() {
        AsPrecondition.rejectIf(true, "Message Reported");
    }

    @Test
    public void rejectIfNegativeTest() {
        AsPrecondition.rejectIf(false, MESSAGE_NOT_TO_BE);
    }
}