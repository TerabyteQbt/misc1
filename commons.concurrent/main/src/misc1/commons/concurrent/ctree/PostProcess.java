package misc1.commons.concurrent.ctree;

import com.google.common.collect.ImmutableList;
import misc1.commons.Either;

interface PostProcess<V> {
    Either<V, ComputationTree<V>> apply(ImmutableList<Object> childResults);
}
