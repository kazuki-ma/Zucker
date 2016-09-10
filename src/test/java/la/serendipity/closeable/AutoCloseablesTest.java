package la.serendipity.closeable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;

import org.junit.Test;

public class AutoCloseablesTest {
    @Test(expected = InvocationTargetException.class)
    public void testConstructorIsPrivate() throws Exception {
        Constructor<AutoCloseables> constructor = AutoCloseables.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test(expected = NullPointerException.class)
    public void executorServiceNpeTest() {
        AutoCloseables.closeable((ExecutorService) null);
    }
}