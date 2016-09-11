package la.serendipity.closeable;

import java.util.concurrent.ExecutorService;

import org.junit.Test;

import la.serendipity.UtilityClassTest;

public class AutoCloseablesTest extends UtilityClassTest {

    @Test(expected = NullPointerException.class)
    public void executorServiceNpeTest() {
        AutoCloseables.closeable((ExecutorService) null);
    }
}