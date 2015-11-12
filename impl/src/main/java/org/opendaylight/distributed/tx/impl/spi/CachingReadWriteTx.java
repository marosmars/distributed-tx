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

        @SuppressWarnings("unchecked")
        final CheckedFuture<Optional<DataObject>, ReadFailedException> read = delegate
            .read(logicalDatastoreType, (InstanceIdentifier<DataObject>) instanceIdentifier);

        Futures.addCallback(read, new FutureCallback<Optional<DataObject>>() {
            @Override public void onSuccess(final Optional<DataObject> result) {

                if (result.isPresent()) {
                    cache.add(new CachedData(instanceIdentifier, result.get(), ModifyAction.DELETE));
                } else {
                    // Deleting something that wasnt there, rollback will ignore
                    cache.add(new CachedData(instanceIdentifier, result.get(), ModifyAction.DELETE));
                }

                try {
                    delegate.delete(logicalDatastoreType, instanceIdentifier);
                } catch (RuntimeException e) {
                    // FAILURE of edit
                    // TODO
                    throw new TxException("Delete failed", e);
                }
            }

            @Override public void onFailure(final Throwable t) {
                // Mark as failed or notify distributed TX, since we cannot cache data, distributed TX needs to fail
            }
        });
    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t) {
        final CheckedFuture<Optional<T>, ReadFailedException> read = delegate
            .read(logicalDatastoreType, instanceIdentifier);

        Futures.addCallback(read, new FutureCallback<Optional<T>>() {
            @Override public void onSuccess(final Optional<T> result) {
                cache.add(new CachedData(instanceIdentifier, result.get(), ModifyAction.MERGE));

                try {
                    delegate.merge(logicalDatastoreType, instanceIdentifier, t);
                } catch (RuntimeException e) {
                    // FAILURE of edit
                    // TODO
                    throw new TxException("Merge failed", e);
                }
            }

            @Override public void onFailure(final Throwable t) {
                // Mark as failed or notify distributed TX, since we cannot cache data, distributed TX needs to fail
            }
        });
    }

    @Deprecated
    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b) {
        delegate.merge(logicalDatastoreType, instanceIdentifier, t, b);
        // TODO how to handle ensure parents ? we dont have control over that here, so we probably have to cache the whole subtree
        // Not support it.
    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t) {
        final CheckedFuture<Optional<T>, ReadFailedException> read = delegate
                .read(logicalDatastoreType, instanceIdentifier);

        Futures.addCallback(read, new FutureCallback<Optional<T>>() {
            @Override public void onSuccess(final Optional<T> result) {
                cache.add(new CachedData(instanceIdentifier, result.get(), ModifyAction.REPLACE));

                try {
                    delegate.put(logicalDatastoreType, instanceIdentifier, t);
                } catch (RuntimeException e) {
                    // FAILURE of edit
                    // TODO
                    throw new TxException("Put failed", e);
                }
            }

            @Override public void onFailure(final Throwable t) {
                // Mark as failed or notify distributed TX, since we cannot cache data, distributed TX needs to fail
            }
        });
    }

    @Deprecated
    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean ensureParents) {
        delegate.put(logicalDatastoreType, instanceIdentifier, t, ensureParents);
        // TODO ERUAN
        // TODO How do we handle this ?
        // Either ignore ensure parents and log warning that ensure parents is unsupported
        // Or throw an exception (but that means rollback) unless we wont do auto-rollback on errors during edit
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
