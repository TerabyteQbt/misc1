package misc1.commons;

import misc1.commons.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;

public final class ExceptionUtilsTest {

    @Test
    public void testCommuteWrapsExceptions() {
        MyException myException = new MyException();
        try {
            throw ExceptionUtils.commute(myException);
        }
        catch(RuntimeException rte) {
            Assert.assertEquals("should wrap checked exception", myException, rte.getCause());
        }
    }

    @Test
    public void testCommuteDoesNotWrapRuntimeExceptions() {
        MyRuntimeException myRuntimeException = new MyRuntimeException();
        try {
            throw ExceptionUtils.commute(myRuntimeException);
        }
        catch(RuntimeException rte) {
            Assert.assertEquals("shouldn't wrap RuntimeException subclass", myRuntimeException, rte);
        }
    }

    private final class MyRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    private final class MyException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
