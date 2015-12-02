package org.opendaylight.distributed.tx.impl;

import com.google.common.util.concurrent.CheckedFuture;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.distributed.tx.api.DTxException;
import org.opendaylight.distributed.tx.impl.spi.DtxImpl;
import org.opendaylight.distributed.tx.spi.TxException;
import org.opendaylight.distributed.tx.spi.TxProvider;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DtxImplTest{
    DtxImpl dtxImpl;
    InstanceIdentifier<TestClassNode1> n1;
    InstanceIdentifier<TestClassNode2> n2;
    InstanceIdentifier<TestClassNode> n0;

    private class myTxProvider implements TxProvider{
        @Override
        public ReadWriteTransaction newTx(InstanceIdentifier<?> nodeId) throws TxException.TxInitiatizationFailedException {
            return new DTXTestTransaction();
        }
    }

    private class TestClassNode implements DataObject{
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    private class TestClassNode1 extends TestClassNode implements DataObject{

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }
    private class TestClassNode2 extends TestClassNode implements DataObject{

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    private void testInit(){
        Set s = new HashSet<InstanceIdentifier<TestClassNode>>();
        this.n1 = InstanceIdentifier.create(TestClassNode1.class);
        s.add(this.n1);
        this.n2 = InstanceIdentifier.create(TestClassNode2.class);
        s.add(n2);
        this.n0 = InstanceIdentifier.create(TestClassNode.class);

        dtxImpl = new DtxImpl(new myTxProvider(), s);
    }

    @Before
    public void testConstructor() {
        testInit();
    }

    @Test
    public void testPutAndRollbackOnFailure(){
        Assert.assertNotNull(this.dtxImpl);

        CheckedFuture<Void, ReadFailedException> f = this.dtxImpl.putAndRollbackOnFailure(LogicalDatastoreType.OPERATIONAL, n0, new TestClassNode(), n1);
    }

    @Test
    public void testMergeAndRollbackOnFailure(){
        Assert.assertNotNull(this.dtxImpl);

        CheckedFuture<Void, ReadFailedException> f = this.dtxImpl.mergeAndRollbackOnFailure(LogicalDatastoreType.OPERATIONAL, n0, new TestClassNode(), n1);
    }

    @Test
    public void testDeleteAndRollbackOnFailure(){
        Assert.assertNotNull(this.dtxImpl);

        CheckedFuture<Void, ReadFailedException> f = this.dtxImpl.deleteAndRollbackOnFailure(LogicalDatastoreType.OPERATIONAL, n0, n1);
    }

    @Test public void testPut() {
        Assert.assertNotNull(this.dtxImpl);
        this.dtxImpl.put(LogicalDatastoreType.OPERATIONAL, n0, new TestClassNode(), n1);
    }
    @Test public void testMerge() {
        Assert.assertNotNull(this.dtxImpl);
        this.dtxImpl.merge(LogicalDatastoreType.OPERATIONAL, n0, new TestClassNode(), n1);
    }
    @Test public void testDelete() {
        Assert.assertNotNull(this.dtxImpl);
        this.dtxImpl.delete(LogicalDatastoreType.OPERATIONAL, n0, n1);
    }
    @Test public void testRollback(){
        Assert.assertNotNull(this.dtxImpl);
        this.testPut();
        this.testMerge();
        this.testDelete();
        // CheckedFuture<Void, DTxException.RollbackFailedException> f = this.dtxImpl.rollback();
    }
    @Test
    public void testSubmit() {
        Assert.assertNotNull(this.dtxImpl);

        this.testPut();
        this.testMerge();
        this.testDelete();

        // CheckedFuture<Void, TransactionCommitFailedException> f = this.dtxImpl.submit();
    }
}
