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
package misc1.commons.concurrent.asyncupdater;

/**
 * An instance of some type of low-QoS notification.
 */
public interface KeylessAsyncUpdate<A> {
    /**
     * Merge this with other.  Both this and other must belong to the caller
     * and will no longer belong to the caller after this method (unless one is
     * returned).
     */
    A merge(A other);

    /**
     * Actually perform whatever blocking action is required to relay the
     * update.
     */
    void fire();
}
