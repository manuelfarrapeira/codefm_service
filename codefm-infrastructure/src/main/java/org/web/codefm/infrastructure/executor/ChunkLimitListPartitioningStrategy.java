package org.web.codefm.infrastructure.executor;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ChunkLimitListPartitioningStrategy implements ListPartitioningStrategy {

    private final ListPartitioningPropertyProvider listPartitioningPropertyProvider;

    @Builder
    public ChunkLimitListPartitioningStrategy(final ListPartitioningPropertyProvider listPartitioningPropertyProvider) {

        Assert.notNull(listPartitioningPropertyProvider, "listPartitioningPropertyProvider must not be null");
        Assert.isTrue(listPartitioningPropertyProvider.getPartitionLimit() > 0, "partitionLimit must be higher than zero");

        this.listPartitioningPropertyProvider = listPartitioningPropertyProvider;
    }

    /**
     * Splits a given list into smaller partitions based on a predefined partition limit.
     * If the remainder of the last partition is smaller than half of the partition limit,
     * it merges the remainder with the previous partition to reduce fragmented data.
     *
     * @param listToPartition the list to be partitioned; must not be null
     * @param <T>             the type of elements in the list
     * @return a list of sublists (partitions) where each sublist's size does not exceed the partition limit
     */
    @Override
    public <T> List<List<T>> split(final List<T> listToPartition) {

        Assert.notNull(listToPartition, "listToPartition must not be null");

        final Integer partitionLimit = this.listPartitioningPropertyProvider.getPartitionLimit();

        final int listSize = listToPartition.size();
        final int completedChunks = listSize / partitionLimit;
        final int remainder = listSize % partitionLimit;
        final int factorNextChunk = partitionLimit / 2;

        log.info("partitionLimit: {}, listSize: {}, completedChunks: {}, remainder: {}, factorNextChunk: {}", partitionLimit, listSize,
                completedChunks, remainder,
                factorNextChunk);

        final List<List<T>> partitionList = Lists.partition(listToPartition, partitionLimit).stream().collect(Collectors.toList());
        final int partitionListSize = partitionList.size();

        if (completedChunks > 0 && remainder > 0 && remainder <= factorNextChunk) {
            final List<T> lastList = partitionList.get(partitionListSize - 1);
            final List<T> newLastList = new ArrayList<>(partitionList.get(partitionListSize - 2));

            newLastList.addAll(lastList);

            partitionList.removeAll(partitionList.subList(partitionListSize - 2, partitionListSize));
            partitionList.add(newLastList);
        }

        log.info("number of partitions: {}", partitionList.size());

        return partitionList;
    }
}
