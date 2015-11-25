package org.opendaylight.distributed.tx.spi;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * APIs for a caching transaction. Providing cached data to Rollback execution.
 */
public interface TxCache extends Iterable<CachedData> {

    public CheckedFuture<Void, ReadFailedException> asyncDelete(final LogicalDatastoreType logicalDatastoreType,
                                                                final InstanceIdentifier<?> instanceIdentifier);


}
