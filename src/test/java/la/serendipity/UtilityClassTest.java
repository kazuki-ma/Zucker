package la.serendipity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

public abstract class UtilityClassTest {
    @Test
    public void testConstructorIsPrivate() throws Exception {
        final Class<?> targetClass = Class.forName(this.getClass().getCanonicalName().replaceAll("Test$", ""));

        Constructor<?> constructor = targetClass.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);

        assertThatExceptionOfType(InvocationTargetException.class)
                .isThrownBy(constructor::newInstance);
    }
}
