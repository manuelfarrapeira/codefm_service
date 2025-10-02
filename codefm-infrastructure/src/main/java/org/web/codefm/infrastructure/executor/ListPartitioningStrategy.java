package org.web.codefm.infrastructure.executor;

import java.util.List;

public interface ListPartitioningStrategy {

    <T> List<List<T>> split(final List<T> listToPartition);

}
