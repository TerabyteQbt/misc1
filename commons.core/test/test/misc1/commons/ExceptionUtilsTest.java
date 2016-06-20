//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
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
