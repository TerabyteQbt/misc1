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

public class OptionsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int priority;

    public OptionsException(String message) {
        this(0, message);
    }

    public OptionsException(int priority, String message) {
        super(message);

        this.priority = priority;
    }

    public OptionsException join(OptionsException other) {
        if(other.priority > priority) {
            return other;
        }
        return this;
    }
}
