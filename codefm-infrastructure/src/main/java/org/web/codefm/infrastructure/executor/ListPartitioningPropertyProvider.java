package org.web.codefm.infrastructure.executor;

/**
 * Supplier of properties which different partitioning strategies need
 */
public interface ListPartitioningPropertyProvider {

    Integer getPartitionLimit();

}
