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
