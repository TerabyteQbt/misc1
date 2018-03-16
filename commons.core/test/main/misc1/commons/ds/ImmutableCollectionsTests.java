package misc1.commons.ds;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableCollectionsTests {
    private static final int MAX_A = 100;
    private static final int MAX_B = 10;
    private static final int MAX_REPS = 100000;

    private static class Key {
        private final int a;
        private final int b;

        public Key(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return this.a;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if(a != other.a) {
                return false;
            }
            if(b != other.b) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "(" + a + "," + b + ")";
        }
    }

    @Test
    public void testMap() {
        List<Key> allKeys = Lists.newArrayList();
        for(int a = 0; a < MAX_A; ++a) {
            for(int b = 0; b < MAX_B; ++b) {
                allKeys.add(new Key(a, b));
                allKeys.add(new Key(a, b));
            }
        }
        allKeys.add(null);

        List<String> allValues = Lists.newArrayList();
        allValues.add(null);
        allValues.add("1");
        allValues.add("2");
        allValues.add("3");

        Map<Key, String> expected = Maps.newHashMap();
        ImmutableSalvagingMap<Key, String> observed = ImmutableSalvagingMap.of();

        Random r = new Random(0x1234);
        for(int reps = 0; reps < MAX_REPS; ++reps) {
            switch(r.nextInt(9)) {
                case 0: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    expected.remove(k);
                    observed = observed.simpleRemove(k);
                    break;
                }

                case 1: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    String v = allValues.get(r.nextInt(allValues.size()));
                    expected.put(k, v);
                    observed = observed.simplePut(k, v);
                    break;
                }

                case 2: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    String exp = expected.remove(k);
                    Pair<ImmutableSalvagingMap<Key, String>, String> obs = observed.remove(k);
                    Assert.assertEquals("m.remove(" + k + ") at T = " + reps, exp, obs.getRight());
                    observed = obs.getLeft();
                    break;
                }

                case 3: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    String v = allValues.get(r.nextInt(allValues.size()));
                    String exp = expected.put(k, v);
                    Pair<ImmutableSalvagingMap<Key, String>, String> obs = observed.put(k, v);
                    Assert.assertEquals("put(" + k + ", " + v + ") at T = " + reps, exp, obs.getRight());
                    observed = obs.getLeft();
                    break;
                }

                case 4: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    Assert.assertSame("get(" + k + ") at T = " + reps, expected.get(k), observed.get(k));
                    break;
                }

                case 5: {
                    Assert.assertEquals("size() at T = " + reps, expected.size(), observed.size());
                    Assert.assertEquals("isEmpty() at T = " + reps, expected.isEmpty(), observed.isEmpty());
                    break;
                }

                case 6: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    Assert.assertEquals("containsKey(" + k + ") at T = " + reps, expected.containsKey(k), observed.containsKey(k));
                    break;
                }

                case 7: {
                    String v = allValues.get(r.nextInt(allValues.size()));
                    Assert.assertEquals("containsValue(" + v + ") at T = " + reps, expected.containsValue(v), observed.containsValue(v));
                    break;
                }

                case 8: {
                    if(r.nextInt(20) == 0) {
                        Assert.assertEquals("entries() at T = " + reps, expected.entrySet(), ImmutableSet.copyOf(observed.entries()));
                        Assert.assertEquals("keys() at T = " + reps, expected.keySet(), Sets.newHashSet(observed.keys()));
                    }
                    break;
                }
            }
        }
    }

    @Test
    public void testMapHceSimple() {
        ImmutableList.Builder<ImmutableSalvagingMap<Key, String>> b = ImmutableList.builder();
        b.add(ImmutableSalvagingMap.<Key, String>of());
        b.add(ImmutableSalvagingMap.<Key, String>of().simplePut(new Key(0, 0), "a"));
        b.add(ImmutableSalvagingMap.<Key, String>of().simplePut(new Key(0, 0), "a").simplePut(new Key(0, 1), "b"));
        b.add(ImmutableSalvagingMap.<Key, String>of().simplePut(new Key(0, 0), "a").simplePut(new Key(1, 0), "c"));
        b.add(ImmutableSalvagingMap.<Key, String>of().simplePut(new Key(0, 0), "b"));
        ImmutableList<ImmutableSalvagingMap<Key, String>> ms = b.build();

        for(int i = 0; i < ms.size(); ++i) {
            for(int j = 0; j < i; ++j) {
                Assert.assertNotEquals(ms.get(i), ms.get(j));
                Assert.assertNotEquals(ms.get(j), ms.get(i));
            }
        }
        for(int i = 0; i < ms.size(); ++i) {
            Assert.assertEquals(ms.get(i), ms.get(i));
        }
    }

    @Test
    public void testMapHceBig() {
        List<Key> allKeys = Lists.newArrayList();
        for(int a = 0; a < MAX_A; ++a) {
            for(int b = 0; b < MAX_B; ++b) {
                allKeys.add(new Key(a, b));
                allKeys.add(new Key(a, b));
            }
        }
        allKeys.add(null);

        Random r = new Random(0x1234);
        for(int reps = 0; reps < MAX_REPS; ++reps) {
            int sz = 10;

            List<Pair<Key, Key>> entries = Lists.newArrayList();
            Set<Key> keys = Sets.newHashSet();
            for(int i = 0; i < sz; ++i) {
                Key k;
                do {
                    k = allKeys.get(r.nextInt(allKeys.size()));
                }
                while(!keys.add(k));
                Key v = allKeys.get(r.nextInt(allKeys.size()));
                entries.add(Pair.of(k, v));
            }

            Collections.shuffle(entries, r);
            ImmutableSalvagingMap<Key, Key> m1 = ImmutableSalvagingMap.<Key, Key>of();
            for(Pair<Key, Key> e : entries) {
                m1 = m1.simplePut(e.getKey(), e.getValue());
            }

            Collections.shuffle(entries, r);
            ImmutableSalvagingMap<Key, Key> m2 = ImmutableSalvagingMap.<Key, Key>of();
            for(Pair<Key, Key> e : entries) {
                m2 = m2.simplePut(e.getKey(), e.getValue());
            }

            Assert.assertEquals(m1.hashCode(), m2.hashCode());
            Assert.assertEquals(m1, m2);
            Assert.assertEquals(m2, m1);
        }
    }

    @Test
    public void testSet() {
        List<Key> allKeys = Lists.newArrayList();
        for(int a = 0; a < MAX_A; ++a) {
            for(int b = 0; b < MAX_B; ++b) {
                allKeys.add(new Key(a, b));
                allKeys.add(new Key(a, b));
            }
        }
        allKeys.add(null);

        Set<Key> expected = Sets.newHashSet();
        ImmutableSalvagingSet<Key> observed = ImmutableSalvagingSet.of();

        Random r = new Random(0x1234);
        for(int reps = 0; reps < MAX_REPS; ++reps) {
            switch(r.nextInt(5)) {
                case 0: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    expected.remove(k);
                    observed = observed.remove(k);
                    break;
                }

                case 1: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    expected.add(k);
                    observed = observed.add(k);
                    break;
                }

                case 2: {
                    Assert.assertEquals("size() at T = " + reps, expected.size(), observed.size());
                    Assert.assertEquals("isEmpty() at T = " + reps, expected.isEmpty(), observed.isEmpty());
                    break;
                }

                case 3: {
                    Key k = allKeys.get(r.nextInt(allKeys.size()));
                    Assert.assertEquals("contains(" + k + ") at T = " + reps, expected.contains(k), observed.contains(k));
                    break;
                }

                case 4: {
                    if(r.nextInt(20) == 0) {
                        Assert.assertEquals("[self] at T = " + reps, expected, Sets.newHashSet(observed));
                    }
                    break;
                }
            }
        }
    }

    @Test
    public void testMapEmpty() {
        ImmutableSalvagingMap<Object, Object> m = ImmutableSalvagingMap.of();
        Assert.assertEquals("size()", 0, m.size());
        Assert.assertTrue("isEmpty()", m.isEmpty());
        Assert.assertFalse("containsKey(\"fucko\")", m.containsKey("fucko"));
        Assert.assertFalse("containsKey(null)", m.containsKey(null));
        Assert.assertFalse("containsValue(\"fucko\")", m.containsValue("fucko"));
        Assert.assertFalse("containsValue(null)", m.containsValue(null));
        Assert.assertNull("get(\"fucko\")", m.get("fucko"));
        Assert.assertNull("get(null)", m.get(null));
        Assert.assertEquals("entries()", ImmutableSet.of(), ImmutableSet.copyOf(m.entries()));
        Assert.assertEquals("keys()", ImmutableSet.of(), Sets.newHashSet(m.keys()));
    }

    @Test
    public void testSetEmpty() {
        ImmutableSalvagingSet<Object> s = ImmutableSalvagingSet.of();
        Assert.assertEquals("size()", 0, s.size());
        Assert.assertTrue("isEmpty()", s.isEmpty());
        Assert.assertFalse("contains(\"fucko\")", s.contains("fucko"));
        Assert.assertFalse("contains(null)", s.contains(null));
        Assert.assertEquals("[self]", ImmutableSet.of(), Sets.newHashSet(s));
    }
}
