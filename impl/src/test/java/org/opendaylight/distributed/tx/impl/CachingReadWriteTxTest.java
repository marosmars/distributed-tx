package org.opendaylight.distributed.tx.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.distributed.tx.impl.spi.CachingReadWriteTx;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import javax.annotation.Nullable;

public class CachingReadWriteTxTest {
    DTXTestTransaction testTx;

    @Before
    public void testInit(){ this.testTx = new DTXTestTransaction(); }
    @Test
    public void testConstructor() {
        new CachingReadWriteTx(new DTXTestTransaction());
    }

    @Test
    public void testAsyncPut() throws InterruptedException {
        /* FIXME The case should test right read after read in DTXTestTransaction is fixed. */
        testTx.setReadException(true);

        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(testTx);

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            CheckedFuture<Void, ReadFailedException> cf =  cacheRWTx.asyncPut(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());

            while(!cf.isDone() ) {
                Thread.sleep(10);
            }
        }

        Assert.assertEquals("size is wrong", cacheRWTx.getSizeOfCache(), 0);
    }

    @Test
    public void testAsyncMerge(){
        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(new DTXTestTransaction());

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            CheckedFuture<Void, ReadFailedException> cf =  cacheRWTx.asyncMerge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());
        }

        Assert.assertEquals("size is wrong", cacheRWTx.getSizeOfCache(), 0);
    }

    @Test
    public  void testAsyncDelete(){
        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(new DTXTestTransaction());

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            CheckedFuture<Void, ReadFailedException> cf =  cacheRWTx.asyncMerge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());
        }

        int numberOfDeleted = 5;

        for(int i = 0; i < numberOfDeleted; i++){
            CheckedFuture<Void, ReadFailedException> f = cacheRWTx.asyncDelete(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class));
        }

        Assert.assertEquals("size is wrong", cacheRWTx.getSizeOfCache(), 0);
    }

    @Test
    public void testMerge(){
        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(new DTXTestTransaction());

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            cacheRWTx.merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());
        }
        // Assert.assertEquals("size is wrong", cacheRWTx.getSizeOfCache(), numberOfObjs);
    }
    @Test
    public void testPut(){
        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(new DTXTestTransaction());

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            cacheRWTx.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());
        }
    }

    @Test
    public void testDelete(){
        CachingReadWriteTx cacheRWTx = new CachingReadWriteTx(new DTXTestTransaction());

        int numberOfObjs = 10;

        for(int i = 0; i < numberOfObjs; i++){
            CheckedFuture<Void, ReadFailedException> cf =  cacheRWTx.asyncMerge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class), new DTXTestTransaction.myDataObj());
        }

        int numberOfDeleted = 5;

        for(int i = 0; i < numberOfDeleted; i++){
            cacheRWTx.delete(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(DTXTestTransaction.myDataObj.class));
        }
    }
    @Test
    public void testConcurrentAsyncPut(){
        // This is the test routine of concurrent asyncPut
    }

    @Test
    public void testConcurrentAsyncMerge(){
        // This is the test routine of concurrent asyncMerge
    }

    @Test
    public void testConcurrentAsyncDelete(){
        // This is the test routine of concurrent asyncMerge
    }

    @Test
    public void testAsyncPutWithException(){
        // FIXME
    }

}
