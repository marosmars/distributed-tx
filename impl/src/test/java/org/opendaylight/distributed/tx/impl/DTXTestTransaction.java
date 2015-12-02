package org.opendaylight.distributed.tx.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.antlr.v4.runtime.misc.Nullable;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class DTXTestTransaction implements ReadWriteTransaction {
    boolean readException = false;

    public void setReadException(boolean ept){
        this.readException = ept;
    }
    @Override
    public <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> read(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier) {

        final SettableFuture<Optional<T>> retFuture = SettableFuture.create();
        Runnable readResult = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!readException){

                    //fixme should set the instance of T
                    retFuture.set(null);
                }else
                    retFuture.setException(new Throwable("simulated error"));
                retFuture.notifyAll();
            }
        };

        new Thread(readResult).start();

        Function<Exception, ReadFailedException> f = new Function<Exception, ReadFailedException>() {
            @Nullable
            @Override
            public ReadFailedException apply(@Nullable Exception e) {
                return new ReadFailedException("Merge failed and rollback", e);
            }
        };

        return Futures.makeChecked(retFuture, f);
    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {

    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {

    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t) {

    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<T> instanceIdentifier, T t, boolean b) {

    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void delete(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier) {

    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> submit() {
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<TransactionStatus>> commit() {
        return null;
    }

    @Override
    public Object getIdentifier() {
        return null;
    }
    public static class myDataObj implements DataObject{
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return myDataObj.class;
        }
    }
}
