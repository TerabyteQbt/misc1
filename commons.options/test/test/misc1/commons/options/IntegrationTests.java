package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import misc1.commons.Maybe;
import org.junit.Assert;
import org.junit.Test;

public class IntegrationTests {
    public static class TestStringOptions {
        public static final OptionsFragment<TestStringOptions, ?, String> s1 = new NamedStringSingletonArgumentOptionsFragment<TestStringOptions>(ImmutableList.of("--s1"), Maybe.<String>not(), "s1");
    }

    @Test
    public void testString() {
        OptionsResults<TestStringOptions> result = OptionsResults.parse(TestStringOptions.class, "--s1", "val");
        Assert.assertEquals("val", result.get(TestStringOptions.s1));

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
    }

    public static class TestFlagOptions {
        public static final OptionsFragment<TestFlagOptions, ?, Boolean> f = new NamedBooleanFlagOptionsFragment<TestFlagOptions>(ImmutableList.of("-f"), "f");
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
        public static final OptionsFragment<TestUnparsedHardOptions, ?, Boolean> f = new NamedBooleanFlagOptionsFragment<TestUnparsedHardOptions>(ImmutableList.of("-f"), "f");
        public static final OptionsFragment<TestUnparsedHardOptions, ?, ImmutableList<String>> x = new UnparsedOptionsFragment<TestUnparsedHardOptions>("x", true, null, null);
    }

    public static class TestUnparsedSoftOptions {
        public static final OptionsFragment<TestUnparsedSoftOptions, ?, Boolean> f = new NamedBooleanFlagOptionsFragment<TestUnparsedSoftOptions>(ImmutableList.of("-f"), "f");
        public static final OptionsFragment<TestUnparsedSoftOptions, ?, ImmutableList<String>> x = new UnparsedOptionsFragment<TestUnparsedSoftOptions>("x", false, null, null);
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
        public static final OptionsFragment<TestCombinedOptions, ?, String> s1 = new NamedStringSingletonArgumentOptionsFragment<TestCombinedOptions>(ImmutableList.of("--s1"), Maybe.<String>not(), "s1");
        public static final OptionsFragment<TestCombinedOptions, ?, Boolean> f = new NamedBooleanFlagOptionsFragment<TestCombinedOptions>(ImmutableList.of("-f"), "f");
        public static final OptionsFragment<TestCombinedOptions, ?, ImmutableList<String>> x = new UnparsedOptionsFragment<TestCombinedOptions>("x", true, null, null);
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
}
