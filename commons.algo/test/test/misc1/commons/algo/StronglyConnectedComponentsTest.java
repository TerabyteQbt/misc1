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
package misc1.commons.algo;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class StronglyConnectedComponentsTest {
    @Test
    public void test1() {
        Random r = new Random(1);
        for(int i = 0; i < 1000; ++i) {
            test1(r.nextLong());
        }
    }

    private void test1(long seed) {
        final Random r = new Random(seed);

        final ImmutableMultimap<String, String> links;
        {
            ImmutableMultimap.Builder<String, String> b = ImmutableMultimap.builder();

            b.put("a", "e");
            b.put("b", "a");
            b.put("c", "b");
            b.put("c", "d");
            b.put("d", "c");
            b.put("e", "b");
            b.put("f", "b");
            b.put("f", "e");
            b.put("f", "g");
            b.put("g", "f");
            b.put("g", "c");
            b.put("h", "g");
            b.put("h", "d");
            b.put("h", "h");

            links = b.build();
        }

        StronglyConnectedComponents<String> algo = new StronglyConnectedComponents<String>() {
            @Override
            protected Iterable<String> getLinks(String v) {
                List<String> ret = Lists.newArrayList(links.get(v));
                Collections.shuffle(ret, r);
                return ret;
            }
        };

        List<String> vertices = Lists.newArrayList("a", "b", "c", "d", "e", "f", "g", "h");
        Collections.shuffle(vertices, r);
        for(String v : vertices) {
            algo.compute(v);
        }

        StronglyConnectedComponents.Component<String> ca = algo.compute("a");
        StronglyConnectedComponents.Component<String> cb = algo.compute("b");
        StronglyConnectedComponents.Component<String> cc = algo.compute("c");
        StronglyConnectedComponents.Component<String> cd = algo.compute("d");
        StronglyConnectedComponents.Component<String> ce = algo.compute("e");
        StronglyConnectedComponents.Component<String> cf = algo.compute("f");
        StronglyConnectedComponents.Component<String> cg = algo.compute("g");
        StronglyConnectedComponents.Component<String> ch = algo.compute("h");

        Assert.assertEquals(ca, cb);
        Assert.assertEquals(ca, ce);
        Assert.assertEquals(cc, cd);
        Assert.assertEquals(cf, cg);

        Assert.assertNotEquals(ca, cc);
        Assert.assertNotEquals(ca, cf);
        Assert.assertNotEquals(ca, ch);
        Assert.assertNotEquals(cc, cf);
        Assert.assertNotEquals(cc, ch);
        Assert.assertNotEquals(cf, ch);

        Assert.assertEquals(ImmutableSet.of("a", "b", "e"), ImmutableSet.copyOf(ca.vertices));
        Assert.assertEquals(ImmutableSet.of("c", "d"), ImmutableSet.copyOf(cc.vertices));
        Assert.assertEquals(ImmutableSet.of("f", "g"), ImmutableSet.copyOf(cf.vertices));
        Assert.assertEquals(ImmutableSet.of("h"), ImmutableSet.copyOf(ch.vertices));

        Assert.assertEquals(ImmutableSet.of(), ImmutableSet.copyOf(algo.getLinks(ca)));
        Assert.assertEquals(ImmutableSet.of(ca), ImmutableSet.copyOf(algo.getLinks(cc)));
        Assert.assertEquals(ImmutableSet.of(ca, cc), ImmutableSet.copyOf(algo.getLinks(cf)));
        Assert.assertEquals(ImmutableSet.of(cc, cf), ImmutableSet.copyOf(algo.getLinks(ch)));
    }
}
