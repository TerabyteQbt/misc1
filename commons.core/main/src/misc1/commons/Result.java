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
package misc1.commons;

import com.google.common.base.Function;
import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class Result<VTYPE> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Result() {
    }

    private static class SuccessResult<VTYPE> extends Result<VTYPE> {
        private static final long serialVersionUID = 1L;

        private final VTYPE ret;

        public SuccessResult(VTYPE ret) {
            this.ret = ret;
        }

        @Override
        public String toString() {
            return "SuccessResult(" + ret + ")";
        }

        @Override
        public boolean hasThrowable() {
            return false;
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        public <ETYPE extends Throwable> Result<VTYPE> maybeThrow(Class<ETYPE> clazz) throws ETYPE {
            return this;
        }

        @Override
        public VTYPE get() throws Throwable {
            return ret;
        }

        @Override
        public VTYPE getCommute() {
            return ret;
        }

        @Override
        public VTYPE getOr(VTYPE def) {
            return ret;
        }

        @Override
        public <MTYPE> MTYPE match(Matcher<? super VTYPE, MTYPE> matcher) {
            return matcher.success(ret);
        }

        @Override
        public <VTYPE2> Result<VTYPE2> transform(Function<VTYPE, VTYPE2> function) {
            return new SuccessResult<VTYPE2>(function.apply(ret));
        }
    }

    private static class FailureResult<VTYPE> extends Result<VTYPE> {
        private static final long serialVersionUID = 1L;

        private final Throwable t;

        public FailureResult(Throwable t) {
            this.t = t;
        }

        @Override
        public String toString() {
            return "FailureResult(" + t + ")";
        }

        @Override
        public boolean hasThrowable() {
            return true;
        }

        @Override
        public Throwable getThrowable() {
            return t;
        }

        @Override
        public <ETYPE extends Throwable> Result<VTYPE> maybeThrow(Class<ETYPE> clazz) throws ETYPE {
            if(clazz.isInstance(t)) {
                throw clazz.cast(t);
            }
            return this;
        }

        @Override
        public VTYPE get() throws Throwable {
            throw t;
        }

        @Override
        public VTYPE getCommute() {
            throw ExceptionUtils.commute(t);
        }

        @Override
        public VTYPE getOr(VTYPE def) {
            return def;
        }

        @Override
        public <MTYPE> MTYPE match(Matcher<? super VTYPE, MTYPE> matcher) {
            return matcher.failure(t);
        }

        @Override
        public <VTYPE2> Result<VTYPE2> transform(Function<VTYPE, VTYPE2> function) {
            return new FailureResult<VTYPE2>(t);
        }
    }

    public static <VTYPE> Result<VTYPE> newSuccess(VTYPE ret) {
        return new SuccessResult<VTYPE>(ret);
    }

    public static <VTYPE> Result<VTYPE> newFailure(Throwable t) {
        return new FailureResult<VTYPE>(t);
    }

    private static final Result<Object> NULL_RESULT = Result.<Object>newSuccess(null);
    @SuppressWarnings("unchecked")
    public static <T> Result<T> newSuccess() {
        return (Result<T>) NULL_RESULT;
    }

    public static <T> Result<T> newFromCallable(Callable<T> c) {
        if(c == null) {
            return Result.newSuccess(null);
        }
        try {
            return Result.newSuccess(c.call());
        }
        catch(Exception t) {
            return Result.newFailure(t);
        }
    }

    public interface Matcher<VTYPE, MTYPE> {
        public MTYPE success(VTYPE value);
        public MTYPE failure(Throwable error);
    }

    public abstract boolean hasThrowable();
    public abstract Throwable getThrowable();
    public abstract <ETYPE extends Throwable> Result<VTYPE> maybeThrow(Class<ETYPE> clazz) throws ETYPE;
    public abstract VTYPE get() throws Throwable;
    public abstract VTYPE getCommute();
    public abstract VTYPE getOr(VTYPE def);
    public abstract <MTYPE> MTYPE match(Matcher<? super VTYPE, MTYPE> matcher);
    public abstract <VTYPE2> Result<VTYPE2> transform(Function<VTYPE, VTYPE2> function);
}
