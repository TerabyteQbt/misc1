package misc1.commons;

import org.junit.Assert;
import org.junit.Test;

public final class MaybeTest {
    @Test
    public void testMaybe() {
        Assert.assertFalse("maybe not should not be present", Maybe.not().isPresent());
        Assert.assertEquals("empty maybe should contain null", null, Maybe.of().get(500));
        Assert.assertEquals("should use default if not present", 5, Maybe.not().get(5));
        Assert.assertEquals("shouldn't use default if present", "five", Maybe.of("five").get("four"));
    }
}
