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
package misc1.commons.concurrent.treelock;

import com.google.common.base.Splitter;
import java.util.Iterator;

public class ArrayTreeLockPath<K> implements TreeLockPath<K, ArrayTreeLockPath<K>> {
    private final K car;
    private final ArrayTreeLockPath<K> cdr;

    public ArrayTreeLockPath(K car, ArrayTreeLockPath<K> cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    @Override
    public K car() {
        return car;
    }

    @Override
    public ArrayTreeLockPath<K> cdr() {
        return cdr;
    }

    public static <K> ArrayTreeLockPath<K> of() {
        return null;
    }

    public static <K> ArrayTreeLockPath<K> of(K[] path) {
        return of(path, 0, path.length);
    }

    public static <K> ArrayTreeLockPath<K> of(K[] path, int start, int end) {
        if(start == end) {
            return null;
        }
        return new ArrayTreeLockPath<K>(path[start], of(path, start + 1, end));
    }

    public static <K> ArrayTreeLockPath<K> of(Iterator<K> iterator) {
        if(!iterator.hasNext()) {
            return null;
        }
        return new ArrayTreeLockPath<K>(iterator.next(), of(iterator));
    }

    public static <K> ArrayTreeLockPath<K> of(Iterable<K> iterable) {
        return of(iterable.iterator());
    }

    public static ArrayTreeLockPath<String> split(String string, char delim) {
        if(string.length() == 0) {
            // Arggh split!  It claims "removes trailing empties" but also "on
            // no match be a singleton of string itself".  Behaviour honors
            // latter over former but that's not what we want.
            return of();
        }
        return of(Splitter.on(delim).split(string));
    }
}
