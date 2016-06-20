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

public final class OptionsFragment<O, R> {
    final OptionsFragmentInternals<O, ?, R> delegate;

    OptionsFragment(OptionsFragmentInternals<O, ?, R> delegate) {
        this.delegate = delegate;
    }

    public OptionsFragment<O, R> helpDesc(String helpDesc) {
        return new OptionsFragment<O, R>(delegate.helpDesc(helpDesc));
    }

    public <R2> OptionsFragment<O, R2> transform(OptionsTransform<R, R2> f) {
        return new OptionsFragment<O, R2>(delegate.transform(f));
    }
}
