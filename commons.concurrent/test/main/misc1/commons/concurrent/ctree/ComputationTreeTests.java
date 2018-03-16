package misc1.commons.concurrent.ctree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import misc1.commons.concurrent.TestLatches;
import misc1.commons.concurrent.TestThreads;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class ComputationTreeTests {
    @Test
    public void testSimple() throws InterruptedException {
        TestThreads tt = new TestThreads();
        ComputationTreeComputer ctc = new ComputationTreeComputer(tt::start);
        ComputationTree<String> ctA = ComputationTree.constant("A");
        ComputationTree<String> ctB = ComputationTree.constant("B");
        ComputationTree<String> ctAB = ComputationTree.tuple(ctA, ctB, (a, b) -> a + b);
        Assert.assertEquals("AB", ctc.await(ctAB).getCommute());
        tt.join();
    }

    @Test
    public void testParallel() throws InterruptedException {
        TestThreads tt = new TestThreads();
        TestLatches tl = new TestLatches();
        ComputationTreeComputer ctc = new ComputationTreeComputer(tt::start);
        ComputationTree<String> ctA = ComputationTree.constant("A").transform((a) -> {
            tl.signal("start-a");
            tl.awaitCommute("start-b");
            return a + "X";
        });
        ComputationTree<String> ctB = ComputationTree.constant("B").transform((b) -> {
            tl.signal("start-b");
            tl.awaitCommute("start-a");
            return b + "X";
        });
        ComputationTree<String> ctAB = ComputationTree.tuple(ctA, ctB, (a, b) -> a + b);
        Assert.assertEquals("AXBX", ctc.await(ctAB).getCommute());
        tt.join();
    }

    @Test
    public void testComplex() throws InterruptedException {
        for(int i = 0; i < 100; ++i) {
            testComplex(0x1234 + i);
        }
    }

    private void testComplex(int seed) {
        Random r = new Random(seed);
        TestThreads tt = new TestThreads();
        TestLatches tl = new TestLatches();
        ComputationTreeComputer ctc = new ComputationTreeComputer(tt::start);
        List<Pair<ComputationTree<Integer>, Integer>> l = Lists.newArrayList();
        int nextV = 0;
        for(int i = 0; i < 10; ++i) {
            int v = nextV++;
            l.add(Pair.of(ComputationTree.constant(v), v));
            tl.signal("start-" + v);
        }
        for(int i = 0; i < 300; ++i) {
            ImmutableList<Pair<ComputationTree<Integer>, Integer>> lCopy = ImmutableList.copyOf(l);
            int dest = r.nextInt(l.size());
            Pair<ComputationTree<Integer>, Integer> p1 = l.get(dest);
            Pair<ComputationTree<Integer>, Integer> p2 = l.get(r.nextInt(l.size()));
            Pair<ComputationTree<Integer>, Integer> p3 = l.get(r.nextInt(l.size()));
            Pair<ComputationTree<Integer>, Integer> p4 = l.get(r.nextInt(l.size()));
            int vn = nextV++;
            ComputationTree<Integer> pn = ComputationTree.tuple(p1.getLeft(), p2.getLeft(), p3.getLeft(), p4.getLeft(), (v1, v2, v3, v4) -> {
                // Make sure we got the right values.  There is next to no
                // chance for them to have been provided to us other than by
                // having been run.
                Assert.assertEquals(p1.getRight(), v1);
                Assert.assertEquals(p2.getRight(), v2);
                Assert.assertEquals(p3.getRight(), v3);
                Assert.assertEquals(p4.getRight(), v4);

                // Since our parallelism is arbitrarily wide, all these things
                // should be able to start.  There are actually likely even
                // more things (created after us) that should be unblocked by
                // us but it would be harder to test.
                for(Pair<ComputationTree<Integer>, Integer> p : lCopy) {
                    tl.awaitCommute("start-" + p.getRight());
                }
                tl.signal("start-" + vn);

                return vn;
            });
            l.set(dest, Pair.of(pn, vn));
        }
        for(Pair<ComputationTree<Integer>, Integer> p : l) {
            ctc.start(p.getLeft());
        }
        for(Pair<ComputationTree<Integer>, Integer> p : l) {
            Assert.assertEquals(p.getRight(), ctc.await(p.getLeft()).getCommute());
        }
    }
}
