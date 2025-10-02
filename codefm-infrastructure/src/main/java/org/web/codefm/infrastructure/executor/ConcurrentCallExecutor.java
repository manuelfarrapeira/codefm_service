package org.web.codefm.infrastructure.executor;

import java.util.List;
import java.util.function.Function;

public interface ConcurrentCallExecutor {

    <T, R> List<R> execute(final List<T> listToPartition, final Function<List<T>, List<R>> functionCallAndMap);

}
