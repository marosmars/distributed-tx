package org.opendaylight.distributed.tx.spi;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by cisco on 11/24/15.
 */
public interface  DTXReadWriteTransaction extends  ReadWriteTransaction{

    public <T extends DataObject> CheckedFuture<Void, ReadFailedException> asyncPut(final LogicalDatastoreType logicalDatastoreType,
                                                                                    final InstanceIdentifier<T> instanceIdentifier, final T t) ;
    public <T extends DataObject> CheckedFuture<Void, ReadFailedException>asyncMerge(final LogicalDatastoreType logicalDatastoreType,
                                                                                     final InstanceIdentifier<T> instanceIdentifier, final T t) ;
    public CheckedFuture<Void, ReadFailedException> asyncDelete(final LogicalDatastoreType logicalDatastoreType,
                                                                final InstanceIdentifier<?> instanceIdentifier) ;
}
