package org.opendaylight.distributed.tx.impl.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.distributed.tx.api.DTx;
import org.opendaylight.distributed.tx.api.DTxException;
import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.distributed.tx.spi.TxProvider;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTxProviderImpl implements DTxProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DTxProviderImpl.class);

    private final Set<InstanceIdentifier<?>> devicesInUse = Sets.newHashSet();
    private final Map<Object, DtxReleaseWrapper> currentTxs = Maps.newHashMap();
    private final TxProvider txProvider;

    public DTxProviderImpl(@Nonnull final TxProvider txProvider) {
        this.txProvider = txProvider;
    }

    @Nonnull @Override public synchronized DTx newTx(@Nonnull final Set<InstanceIdentifier<?>> nodes)
        throws DTxException.DTxInitializationFailedException {

        final Sets.SetView<InstanceIdentifier<?>> lockedDevices = Sets.intersection(devicesInUse, nodes);
        if(!lockedDevices.isEmpty()) {
            LOG.warn("Unable to lock nodes(in use): {}", lockedDevices);
            throw new DTxException.DTxInitializationFailedException("Unable to lock nodes(in use): " + lockedDevices);
        }

        devicesInUse.addAll(nodes);
        LOG.debug("Locking nodes for distributed transaction: {}", nodes);
        final DtxReleaseWrapper dtxReleaseWrapper = new DtxReleaseWrapper(new DtxImpl(txProvider, nodes), nodes);
        currentTxs.put(dtxReleaseWrapper.getIdentifier(), dtxReleaseWrapper);
        return dtxReleaseWrapper;
    }

    @Override public void close() throws Exception {
        for (Map.Entry<Object, DtxReleaseWrapper> outstandingTx : currentTxs.entrySet()) {
            LOG.warn("Cancelling outstanding distributed transaction: {}", outstandingTx.getKey());
            outstandingTx.getValue().cancel();
        }

        // TODO check if the collections are empty
    }

    private final class DtxReleaseWrapper implements DTx {

        private final DTx delegate;
        private Set<InstanceIdentifier<?>> nodes;

        private DtxReleaseWrapper(final DTx delegate, final Set<InstanceIdentifier<?>> nodes) {
            this.delegate = delegate;
            this.nodes = nodes;
        }

        private void releaseNodes() {
            synchronized (DTxProviderImpl.this) {
                devicesInUse.removeAll(nodes);
                Preconditions.checkNotNull(currentTxs.remove(getIdentifier()), "Unable to cleanup distributed transaction");
            }
        }

        @Override public boolean cancel() throws DTxException.RollbackFailedException {
            final boolean cancel = delegate.cancel();
            releaseNodes();
            return cancel;
        }

        @Override
        public <T extends DataObject> CheckedFuture<Void, ReadFailedException> mergeAndRollbackOnFailure(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, InstanceIdentifier<?> nodeId) throws DTxException.EditFailedException {
            return delegate.mergeAndRollbackOnFailure(logicalDatastoreType, instanceIdentifier, t, nodeId);
        }

        @Override
        public <T extends DataObject> CheckedFuture<Void, ReadFailedException> putAndRollbackOnFailure(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, InstanceIdentifier<?> nodeId) throws DTxException.EditFailedException {
            return delegate.putAndRollbackOnFailure(logicalDatastoreType, instanceIdentifier, t, nodeId);
        }

        @Override
        public CheckedFuture<Void, ReadFailedException> deleteAndRollbackOnFailure(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier, InstanceIdentifier<?> nodeId) throws DTxException.EditFailedException, DTxException.RollbackFailedException {
            return delegate.deleteAndRollbackOnFailure(logicalDatastoreType, instanceIdentifier, nodeId);
        }

        @Override public void delete(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<?> instanceIdentifier) throws DTxException.EditFailedException {
            delegate.delete(logicalDatastoreType, instanceIdentifier);
        }

        @Override public void delete(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<?> instanceIdentifier, final InstanceIdentifier<?> nodeId)
            throws DTxException.EditFailedException, DTxException.RollbackFailedException {
            delegate.delete(logicalDatastoreType, instanceIdentifier, nodeId);
        }

        @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t)
            throws DTxException.EditFailedException, DTxException.RollbackFailedException {
            delegate.merge(logicalDatastoreType, instanceIdentifier, t);
        }

        @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b)
            throws DTxException.EditFailedException {
            delegate.merge(logicalDatastoreType, instanceIdentifier, t, b);
        }

        @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b,
            final InstanceIdentifier<?> nodeId) throws DTxException.EditFailedException {
            delegate.merge(logicalDatastoreType, instanceIdentifier, t, b, nodeId);
        }

        @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
            throws DTxException.EditFailedException {
            delegate.merge(logicalDatastoreType, instanceIdentifier, t, nodeId);
        }

        @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t) throws DTxException.EditFailedException {
            delegate.put(logicalDatastoreType, instanceIdentifier, t);
        }

        @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b)
            throws DTxException.EditFailedException {
            delegate.put(logicalDatastoreType, instanceIdentifier, t, b);
        }

        @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b,
            final InstanceIdentifier<?> nodeId) throws DTxException.EditFailedException {
            delegate.put(logicalDatastoreType, instanceIdentifier, t, b, nodeId);
        }

        @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
            throws DTxException.EditFailedException {
            delegate.put(logicalDatastoreType, instanceIdentifier, t, nodeId);
        }

        @Override public CheckedFuture<Void, TransactionCommitFailedException> submit()
            throws DTxException.SubmitFailedException, DTxException.RollbackFailedException {
            final CheckedFuture<Void, TransactionCommitFailedException> submit = delegate.submit();

            Futures.addCallback(submit, new FutureCallback<Void>() {
                @Override public void onSuccess(final Void result) {
                    releaseNodes();
                }

                @Override public void onFailure(final Throwable t) {
                    releaseNodes();
                }
            });
            return submit;
        }

        @Deprecated
        @Override public ListenableFuture<RpcResult<TransactionStatus>> commit() {
            throw new UnsupportedOperationException("Deprecated");
        }

        @Override public Object getIdentifier() {
            return delegate.getIdentifier();
        }
    }
}
