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
package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import misc1.commons.Maybe;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class IntegrationTests {
    public static class TestStringOptions {
        public static final OptionsLibrary<TestStringOptions> o = OptionsLibrary.of();
        public static final OptionsFragment<TestStringOptions, String> s1 = o.oneArg("s1").transform(o.singleton());
        public static final OptionsFragment<TestStringOptions, Pair<String, String>> pair = o.twoArg("pair").transform(o.singleton(Pair.of("a", "b")));
    }

    @Test
    public void testString() {
        OptionsResults<TestStringOptions> result = OptionsResults.parse(TestStringOptions.class, "--s1", "val");
        Assert.assertEquals("val", result.get(TestStringOptions.s1));
        Assert.assertEquals(Pair.of("a", "b"), result.get(TestStringOptions.pair));

        result = OptionsResults.parse(TestStringOptions.class, "--s1", "blah", "--pair", "val1", "val2");
        Assert.assertEquals(Pair.of("val1", "val2"), result.get(TestStringOptions.pair));

        try {
            OptionsResults.parse(TestStringOptions.class);
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestStringOptions.class, "fucko");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestStringOptions.class, "--s1", "val1", "--s1", "val2");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestStringOptions.class, "--s1", "val1", "--s1", "val2", "--pair", "justonevalue");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestStringOptions.class, "--pair", "justonevalueinthemiddle", "--s1", "val1", "--s1", "val2");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestStringOptions.class, "--s1", "val1", "--s1", "val2", "--pair");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }
    }

    public static class TestFlagOptions {
        public static final OptionsLibrary<TestFlagOptions> o = OptionsLibrary.of();
        public static final OptionsFragment<TestFlagOptions, Boolean> f = o.zeroArg("f").transform(o.flag());
    }

    @Test
    public void testFlag() {
        Assert.assertEquals(false, OptionsResults.parse(TestFlagOptions.class).get(TestFlagOptions.f));

        Assert.assertEquals(true, OptionsResults.parse(TestFlagOptions.class, "-f").get(TestFlagOptions.f));

        try {
            OptionsResults.parse(TestStringOptions.class, "fucko");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }

        try {
            OptionsResults.parse(TestFlagOptions.class, "-f", "-f");
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }
    }

    public static class TestUnparsedHardOptions {
        public static final OptionsLibrary<TestUnparsedHardOptions> o = OptionsLibrary.of();
        public static final OptionsFragment<TestUnparsedHardOptions, Boolean> f = o.zeroArg("f").transform(o.flag());
        public static final OptionsFragment<TestUnparsedHardOptions, ImmutableList<String>> x = o.unparsed(true);
    }

    public static class TestUnparsedSoftOptions {
        public static final OptionsLibrary<TestUnparsedSoftOptions> o = OptionsLibrary.of();
        public static final OptionsFragment<TestUnparsedSoftOptions, Boolean> f = o.zeroArg("f").transform(o.flag());
        public static final OptionsFragment<TestUnparsedSoftOptions, ImmutableList<String>> x = o.unparsed(false);
    }

    @Test
    public void testUnparsed() {
        Assert.assertEquals(ImmutableList.of(), OptionsResults.parse(TestUnparsedHardOptions.class).get(TestUnparsedHardOptions.x));
        Assert.assertEquals(ImmutableList.of("a"), OptionsResults.parse(TestUnparsedHardOptions.class, "a").get(TestUnparsedHardOptions.x));
        Assert.assertEquals(ImmutableList.of("a", "b"), OptionsResults.parse(TestUnparsedHardOptions.class, "a", "b").get(TestUnparsedHardOptions.x));
        Assert.assertEquals(ImmutableList.of("a"), OptionsResults.parse(TestUnparsedHardOptions.class, "--", "a").get(TestUnparsedHardOptions.x));

        Assert.assertEquals(ImmutableList.of("a", "-f"), OptionsResults.parse(TestUnparsedHardOptions.class, "a", "-f").get(TestUnparsedHardOptions.x));
        Assert.assertEquals(false, OptionsResults.parse(TestUnparsedHardOptions.class, "a", "-f").get(TestUnparsedHardOptions.f));
        Assert.assertEquals(ImmutableList.of("a"), OptionsResults.parse(TestUnparsedSoftOptions.class, "a", "-f").get(TestUnparsedSoftOptions.x));
        Assert.assertEquals(true, OptionsResults.parse(TestUnparsedSoftOptions.class, "a", "-f").get(TestUnparsedSoftOptions.f));

        Assert.assertEquals(ImmutableList.of("a", "-f"), OptionsResults.parse(TestUnparsedHardOptions.class, "--", "a", "-f").get(TestUnparsedHardOptions.x));
        Assert.assertEquals(false, OptionsResults.parse(TestUnparsedHardOptions.class, "--", "a", "-f").get(TestUnparsedHardOptions.f));
        Assert.assertEquals(ImmutableList.of("a", "-f"), OptionsResults.parse(TestUnparsedSoftOptions.class, "--", "a", "-f").get(TestUnparsedSoftOptions.x));
        Assert.assertEquals(false, OptionsResults.parse(TestUnparsedSoftOptions.class, "--", "a", "-f").get(TestUnparsedSoftOptions.f));
    }

    public static class TestCombinedOptions {
        public static final OptionsLibrary<TestCombinedOptions> o = OptionsLibrary.of();
        public static final OptionsFragment<TestCombinedOptions, String> s1 = o.oneArg("s1").transform(o.singleton());
        public static final OptionsFragment<TestCombinedOptions, Boolean> f = o.zeroArg("f").transform(o.flag());
        public static final OptionsFragment<TestCombinedOptions, ImmutableList<String>> x = o.unparsed(true);
    }

    @Test
    public void testCombined() {
        testCombinedFail(ImmutableList.<String>of());
        testCombinedPass(ImmutableList.of("--s1", "-f", "--", "-f"), "-f", false, ImmutableList.of("-f"));
        testCombinedPass(ImmutableList.of("-f", "--s1", "val1"), "val1", true, ImmutableList.<String>of());
        testCombinedFail(ImmutableList.of("-f", "--s1", "val1", "-f"));
        testCombinedPass(ImmutableList.of("--s1", "val1"), "val1", false, ImmutableList.<String>of());
        testCombinedFail(ImmutableList.of("--s1", "val1", "--s1", "val2"));
        testCombinedPass(ImmutableList.of("--s1", "val1", "-f"), "val1", true, ImmutableList.<String>of());
        testCombinedFail(ImmutableList.of("--s1", "val1", "-f", "-f"));
    }

    private void testCombinedFail(ImmutableList<String> args) {
        try {
            OptionsResults.parse(TestCombinedOptions.class, args);
            Assert.fail();
        }
        catch(OptionsException e) {
            // expected
        }
    }

    private void testCombinedPass(ImmutableList<String> args, String s1, boolean f, ImmutableList<String> x) {
        OptionsResults<TestCombinedOptions> r = OptionsResults.parse(TestCombinedOptions.class, args);
        Assert.assertEquals(s1, r.get(TestCombinedOptions.s1));
        Assert.assertEquals(f, r.get(TestCombinedOptions.f));
        Assert.assertEquals(x, r.get(TestCombinedOptions.x));
    }

    public static class TestTrinaryFlag {
        public static final OptionsLibrary<TestTrinaryFlag> o = OptionsLibrary.of();
        public static final OptionsFragment<TestTrinaryFlag, Maybe<Boolean>> flag = o.trinary("flag");
    }

    @Test
    public void testTrinaryFlag() {
        Assert.assertEquals(Maybe.of(true), OptionsResults.parse(TestTrinaryFlag.class, "--flag=true").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.of(false), OptionsResults.parse(TestTrinaryFlag.class, "--flag=false").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.not(), OptionsResults.parse(TestTrinaryFlag.class, "--flag=unset").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.of(true), OptionsResults.parse(TestTrinaryFlag.class, "--flag", "true").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.of(false), OptionsResults.parse(TestTrinaryFlag.class, "--flag", "false").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.not(), OptionsResults.parse(TestTrinaryFlag.class, "--flag", "unset").get(TestTrinaryFlag.flag));
        Assert.assertEquals(Maybe.not(), OptionsResults.parse(TestTrinaryFlag.class).get(TestTrinaryFlag.flag));
    }
}
