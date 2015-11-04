package org.opendaylight.distributed.tx.impl.spi;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.distributed.tx.spi.CachedData;
import org.opendaylight.distributed.tx.spi.TxCache;
import org.opendaylight.distributed.tx.spi.TxException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;

public class CachingReadWriteTx implements TxCache, ReadWriteTransaction, Closeable {

    private final ReadWriteTransaction delegate;
    public final Deque<CachedData> cache = new ConcurrentLinkedDeque<>();

    public CachingReadWriteTx(@Nonnull final ReadWriteTransaction delegate) {
        this.delegate = delegate;
    }

    @Override public Iterator<CachedData> iterator() {
        return cache.descendingIterator();
    }

    @Override public <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> read(
        final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<T> instanceIdentifier) {
        return delegate.read(logicalDatastoreType, instanceIdentifier);
    }

    @Override public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override public void delete(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<?> instanceIdentifier) {
        delegate.delete(logicalDatastoreType, instanceIdentifier);
    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t) {

        final CheckedFuture<Optional<T>, ReadFailedException> read = delegate
            .read(logicalDatastoreType, instanceIdentifier);

        // TODO should we block here ?
        Futures.addCallback(read, new FutureCallback<Optional<T>>() {
            @Override public void onSuccess(final Optional<T> result) {
                cache.add(new CachedData(instanceIdentifier, result.get(), ModifyAction.MERGE));
            }

            @Override public void onFailure(final Throwable t) {
                // Mark as failed or notify distributed TX, since we cannot cache data, distributed TX needs to fail
            }
        });

        try {
            delegate.merge(logicalDatastoreType, instanceIdentifier, t);
        } catch (RuntimeException e) {
            // FAILURE of edit
            // TODO
            throw new TxException("Merge failed", e);
        }
    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b) {
        delegate.merge(logicalDatastoreType, instanceIdentifier, t, b);
        // TODO how to handle ensure parents ? we dont have control over that here, so we probably have to cache the whole subtree
    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t) {
        delegate.put(logicalDatastoreType, instanceIdentifier, t);
    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b) {
        delegate.put(logicalDatastoreType, instanceIdentifier, t, b);
    }

    @Override public boolean cancel() {
        return delegate.cancel();
    }

    @Override public CheckedFuture<Void, TransactionCommitFailedException> submit() {
        return delegate.submit();
    }

    @Deprecated
    @Override public ListenableFuture<RpcResult<TransactionStatus>> commit() {
        return delegate.commit();
    }

    @Override public void close() throws IOException {
        cancel();
        cache.clear();
    }
}
