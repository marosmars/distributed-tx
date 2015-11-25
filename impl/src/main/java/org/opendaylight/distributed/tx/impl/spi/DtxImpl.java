package org.opendaylight.distributed.tx.impl.spi;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.distributed.tx.api.DTx;
import org.opendaylight.distributed.tx.api.DTxException;
import org.opendaylight.distributed.tx.spi.*;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtxImpl implements DTx {

    private static final Logger LOG = LoggerFactory.getLogger(DTxProviderImpl.class);

    private final Map<InstanceIdentifier<?>, CachingReadWriteTx> perNodeTransactions;
    private final TxProvider txProvider;

    public DtxImpl(@Nonnull final TxProvider txProvider, @Nonnull final Set<InstanceIdentifier<?>> nodes) {
        this.txProvider = txProvider;
        Preconditions.checkArgument(!nodes.isEmpty(), "Cannot create distributed tx for 0 nodes");
        perNodeTransactions = initializeTransactions(txProvider, nodes);
    }

    private Map<InstanceIdentifier<?>, CachingReadWriteTx> initializeTransactions(final TxProvider txProvider,
        final Set<InstanceIdentifier<?>> nodes) {

        return Maps.asMap(nodes, new Function<InstanceIdentifier<?>, CachingReadWriteTx>() {
            @Nullable @Override public CachingReadWriteTx apply(@Nullable final InstanceIdentifier<?> input) {
                return new CachingReadWriteTx(txProvider.newTx(input));
            }
        });
    }

    @Override public void delete(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<?> instanceIdentifier, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {

        Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
        final ReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
        try {
            transaction.delete(logicalDatastoreType, instanceIdentifier);
        } catch (final RuntimeException e) {
            Futures.addCallback(this.rollbackUponOperationFaiure(perNodeTransactions, perNodeTransactions), new FutureCallback<Void>(){
                @Override public void onSuccess(@Nullable final Void result) {
                    LOG.info("Distributed tx failed for delete {}. Rollback was successful", instanceIdentifier);
                }

                @Override public void onFailure(final Throwable t) {
                    LOG.warn("Distributed tx filed. Rollback FAILED. Device(s) state is unknown", t);
                    // FIXME No Runtimeexception can be caught when rollback fails. Ignore
                }
            });
            throw new DTxException.EditFailedException("Delete operation failed at node" + nodeId);
        }
    }

    @Override public void delete(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<?> instanceIdentifier) throws DTxException.EditFailedException {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t)
        throws DTxException.EditFailedException, DTxException.RollbackFailedException {
        throw new UnsupportedOperationException("Unimplemented");

    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b)
        throws DTxException.EditFailedException {
        throw new UnsupportedOperationException("Unimplemented");

    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t) throws DTxException.EditFailedException {
        throw new UnsupportedOperationException("Unimplemented");

    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b)
        throws DTxException.EditFailedException {
        throw new UnsupportedOperationException("Unimplemented");

    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {
            Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
            final ReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
            try {
                transaction.merge(logicalDatastoreType, instanceIdentifier, t);
            } catch (final RuntimeException e) {
                Futures.addCallback(this.rollbackUponOperationFaiure(perNodeTransactions, perNodeTransactions), new FutureCallback<Void>(){
                    @Override public void onSuccess(@Nullable final Void result) {
                        LOG.info("Distributed tx failed for merge {}. Rollback was successful", instanceIdentifier);
                    }

                    @Override public void onFailure(final Throwable t) {
                        LOG.warn("Distributed tx failed for merge. Rollback FAILED. Device(s) state is unknown", t);
                        // FIXME No Runtimeexception can be caught when rollback fails. Ignore
                    }
                });
                throw new DTxException.EditFailedException("Put operation failed at node" + nodeId);
            }
    }

    @Override public <T extends DataObject> void merge(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {

    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {
        //FIXME Not thread-safe. Add concurrency protection
        Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
        final ReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
        try {
            transaction.put(logicalDatastoreType, instanceIdentifier, t);
        } catch (final RuntimeException e) {
            Futures.addCallback(this.rollbackUponOperationFaiure(perNodeTransactions, perNodeTransactions), new FutureCallback<Void>(){
                @Override public void onSuccess(@Nullable final Void result) {
                    LOG.info("Distributed tx failed for put{}. Rollback was successful", instanceIdentifier);
                }

                @Override public void onFailure(final Throwable t) {
                    LOG.warn("Distributed tx filed. Rollback FAILED. Device(s) state is unknown", t);

                    // FIXME No Runtimeexception can be caught when rollback fails. Ignore
                }
            });
            throw new DTxException.EditFailedException("Put operation failed at node" + nodeId);
        }
    }

    @Override public <T extends DataObject> void put(final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final boolean b, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {

    }

    @Override public CheckedFuture<Void, TransactionCommitFailedException> submit()
        throws DTxException.SubmitFailedException, DTxException.RollbackFailedException {

        final Map<InstanceIdentifier<?>, PerNodeTxState> commitStatus = Maps.newHashMapWithExpectedSize(perNodeTransactions.size());
        final SettableFuture<Void> distributedSubmitFuture = SettableFuture.create();

        for (final Map.Entry<InstanceIdentifier<?>, CachingReadWriteTx> perNodeTx : perNodeTransactions.entrySet()) {
            final CheckedFuture<Void, TransactionCommitFailedException> submit = perNodeTx.getValue().submit();
            Futures.addCallback(submit, new PerNodeSubmitCallback(commitStatus, perNodeTx, distributedSubmitFuture));
        }

        return Futures.makeChecked(distributedSubmitFuture, new Function<Exception, TransactionCommitFailedException>() {
            @Nullable @Override public TransactionCommitFailedException apply(@Nullable final Exception input) {
                return new TransactionCommitFailedException("Submit failed. Check nested exception for rollback status", input);
            }
        });
    }

    // TODO extract all the anonymous Functions/Callbacks into static classes (maybe constants) if possible

    /**
     * Perform submit rollback with the caches and empty rollback transactions for every node
     */
    private CheckedFuture<Void, DTxException.RollbackFailedException> rollback(
        final Map<InstanceIdentifier<?>, ? extends TxCache> perNodeTransactions,
        final Map<InstanceIdentifier<?>, PerNodeTxState> commitStatus) {
        // TODO Extract into a Rollback factory
        Rollback rollback = new RollbackImpl();
        final ListenableFuture<Void> rollbackFuture = rollback.rollback(perNodeTransactions,
            Maps.transformValues(commitStatus, new Function<PerNodeTxState, ReadWriteTransaction>() {
                @Nullable @Override public ReadWriteTransaction apply(@Nullable final PerNodeTxState input) {
                    return input.getRollbackTx();
                }
            }));

        return Futures.makeChecked(rollbackFuture, new Function<Exception, DTxException.RollbackFailedException>() {
            @Nullable @Override public DTxException.RollbackFailedException apply(@Nullable final Exception input) {
                return new DTxException.RollbackFailedException(input);
            }
        });
    }
    private CheckedFuture<Void, DTxException.RollbackFailedException> rollbackUponOperationFaiure(
            final Map<InstanceIdentifier<?>, ? extends TxCache> perNodeCache,  final Map<InstanceIdentifier<?>, ? extends ReadWriteTransaction> perNodeTx){
        Rollback rollback = new RollbackImpl();
        final ListenableFuture<Void> rollbackFuture= rollback.rollback(perNodeCache,perNodeTransactions);

        return Futures.makeChecked(rollbackFuture, new Function<Exception, DTxException.RollbackFailedException>() {
            @Nullable @Override public DTxException.RollbackFailedException apply(@Nullable final Exception input) {
                return new DTxException.RollbackFailedException(input);
            }
        });
    }

    @Deprecated
    @Override public ListenableFuture<RpcResult<TransactionStatus>> commit() {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override public boolean cancel() throws DTxException.RollbackFailedException {
        return false;
    }

    @Override public Object getIdentifier() {
        return perNodeTransactions.keySet();
    }

    private class PerNodeSubmitCallback implements FutureCallback<Void> {

        // TODO this field should be threadsafe
        private final Map<InstanceIdentifier<?>, PerNodeTxState> commitStatus;
        private final Map.Entry<InstanceIdentifier<?>, CachingReadWriteTx> perNodeTx;
        private final SettableFuture<Void> distributedSubmitFuture;

        public PerNodeSubmitCallback(final Map<InstanceIdentifier<?>, PerNodeTxState> commitStatus,
            final Map.Entry<InstanceIdentifier<?>, CachingReadWriteTx> perNodeTx,
            final SettableFuture<Void> distributedSubmitFuture) {
            this.commitStatus = commitStatus;
            this.perNodeTx = perNodeTx;
            this.distributedSubmitFuture = distributedSubmitFuture;
        }

        /**
         * Callback for per-node transaction submit success
         * Prepare potential rollback per-node transaction (rollback will be performed in a dedicated Tx)
         */
        @Override public void onSuccess(@Nullable final Void result) {
            LOG.trace("Per node tx({}/{}) executed successfully for: {}",
                commitStatus.size(), perNodeTransactions.size(), perNodeTx.getKey());
            try {
                final ReadWriteTransaction readWriteTransaction = txProvider.newTx(perNodeTx.getKey());
                final PerNodeTxState status = PerNodeTxState.createSuccess(readWriteTransaction);
                commitStatus.put(perNodeTx.getKey(), status);
                checkTransactionStatus();
            } catch (TxException.TxInitiatizationFailedException e) {
                handleRollbackTxCreationException(e);
            }
        }

        /**
         * Invoked when this distributed transaction cannot open a post submit transaction (for performing potential rollback)
         */
        private void handleRollbackTxCreationException(final TxException.TxInitiatizationFailedException e) {
            // TODO we should try to rollback at least the nodes we can, not just fail the distributed Tx
            LOG.warn("Unable to create post submit transaction for node: {}. Distributed transaction failing",
                perNodeTx.getKey(), e);
            distributedSubmitFuture.setException(new DTxException.SubmitFailedException(
                Collections.<InstanceIdentifier<?>>singleton(perNodeTx.getKey()), e));
        }

        /**
         * Callback for per-node transaction submit FAIL
         * Prepare rollback per-node transaction (rollback will be performed in a dedicated Tx)
         */
        @Override public void onFailure(final Throwable t) {
            LOG.warn("Per node tx executed failed for: {}", perNodeTx.getKey(), t);
            try {
                final ReadWriteTransaction readWriteTransaction = txProvider.newTx(perNodeTx.getKey());
                commitStatus.put(perNodeTx.getKey(),  PerNodeTxState.createFailed(t, readWriteTransaction));
                checkTransactionStatus();

            } catch (TxException.TxInitiatizationFailedException e) {
                handleRollbackTxCreationException(e);
            }
        }

        /**
         * Check the overall status of distributed Tx after each per-node transaction status change
         */
        private void checkTransactionStatus() {
            try {
                final DistributedSubmitState txState = validate(commitStatus, perNodeTransactions.keySet());

                switch (txState) {
                case WAITING: {
                    return;
                }
                case SUCCESS: {
                    distributedSubmitFuture.set(null);
                    return;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported " + txState);
                }
                }

            } catch (final DTxException.SubmitFailedException e) {
                Futures.addCallback(rollback(perNodeTransactions, commitStatus), new FutureCallback<Void>() {

                    @Override public void onSuccess(@Nullable final Void result) {
                        LOG.info("Distributed tx failed for {}. Rollback was successful", perNodeTx.getKey());
                        distributedSubmitFuture.setException(e);
                    }

                    @Override public void onFailure(final Throwable t) {
                        LOG.warn("Distributed tx filed. Rollback FAILED. Device(s) state is unknown", t);
                        // t should be rollback failed EX
                        distributedSubmitFuture.setException(t);
                    }
                });
            }
        }

        /**
         * Validate distributed Tx status. Either waiting, success-all or fail indicated by an exception
         */
        private DistributedSubmitState validate(final Map<InstanceIdentifier<?>, PerNodeTxState> commitStatus,
            final Set<InstanceIdentifier<?>> instanceIdentifiers) throws DTxException.SubmitFailedException {
            if(commitStatus.size() == instanceIdentifiers.size()) {
                LOG.debug("Distributed tx submit finished with status: {}", commitStatus);
                final Map<InstanceIdentifier<?>, PerNodeTxState> failedSubmits = Maps
                    .filterEntries(commitStatus, new Predicate<Map.Entry<InstanceIdentifier<?>, PerNodeTxState>>() {
                        @Override public boolean apply(final Map.Entry<InstanceIdentifier<?>, PerNodeTxState> input) {
                            return !input.getValue().isSuccess();
                        }
                    });

                if(!failedSubmits.isEmpty()) {
                    throw new DTxException.SubmitFailedException(failedSubmits.keySet());
                } else {
                    return DistributedSubmitState.SUCCESS;
                }
            }

            return DistributedSubmitState.WAITING;
        }

    }

    public enum DistributedSubmitState {
        SUCCESS, FAILED, WAITING;
    }

    /**
     * Per-node transaction state. Generally its success or fail. This also keeps the rollback transaction
     */
    private static final class PerNodeTxState {
        private boolean success;
        @Nullable private Throwable t;
        private final ReadWriteTransaction rollbackTx;

        public PerNodeTxState(final boolean success, @Nullable final Throwable t, @Nonnull ReadWriteTransaction rollbackTx) {
            this(success, rollbackTx);
            this.t = t;
        }

        public PerNodeTxState(final boolean success, @Nonnull ReadWriteTransaction rollbackTx) {
            this.success = success;
            this.rollbackTx = rollbackTx;
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable public Optional<Throwable> getException() {
            return Optional.fromNullable(t);
        }

        public ReadWriteTransaction getRollbackTx() {
            return rollbackTx;
        }

        public static PerNodeTxState createFailed(final Throwable t, final ReadWriteTransaction readWriteTransaction) {
            return new PerNodeTxState(false, t, readWriteTransaction);
        }

        public static PerNodeTxState createSuccess(final ReadWriteTransaction readWriteTransaction) {
            return new PerNodeTxState(true, readWriteTransaction);
        }
    }

    public <T extends DataObject> CheckedFuture<Void, ReadFailedException> mergeAndRollbackOnFailure(
        final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {
            Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
            final DTXReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
            return transaction.asyncMerge(logicalDatastoreType, instanceIdentifier, t);
    }

    public <T extends DataObject> CheckedFuture<Void, ReadFailedException> putAndRollbackOnFailure(
            final LogicalDatastoreType logicalDatastoreType,
            final InstanceIdentifier<T> instanceIdentifier, final T t, final InstanceIdentifier<?> nodeId)
            throws DTxException.EditFailedException {
            Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
            final DTXReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
            return transaction.asyncPut(logicalDatastoreType, instanceIdentifier, t);

    }
    public CheckedFuture<Void, ReadFailedException> deleteAndRollbackOnFailure(
        final LogicalDatastoreType logicalDatastoreType,
        final InstanceIdentifier<?> instanceIdentifier, final InstanceIdentifier<?> nodeId)
        throws DTxException.EditFailedException {
        Preconditions.checkArgument(perNodeTransactions.containsKey(nodeId), "Unknown node: %s. Not in transaction", nodeId);
        final DTXReadWriteTransaction transaction = perNodeTransactions.get(nodeId);
        return transaction.asyncDelete(logicalDatastoreType, instanceIdentifier);
    }
}

