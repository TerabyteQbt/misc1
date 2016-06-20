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
package misc1.commons.tuple;

import java.util.Comparator;
import org.apache.commons.lang3.tuple.Pair;

public final class Misc1PairUtils {
    private Misc1PairUtils() {
        // no
    }

    public static <A, B> Comparator<Pair<A, B>> comparator(final Comparator<? super A> c1, final Comparator<? super B> c2) {
        return (p1, p2) -> {
            int r1 = c1.compare(p1.getLeft(), p2.getLeft());
            if(r1 != 0) {
                return r1;
            }
            return c2.compare(p1.getRight(), p2.getRight());
        };
    }
}
