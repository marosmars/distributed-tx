package org.opendaylight.distributed.tx.spi;

import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Per node transaction provider SPI. Distributed tx treats every node just as an instance of ReadWriteTransaction.
 * This provider interface hides the details of its creation, whether the per node transactions come from MountPoints or are app specific.
 */
public interface TxProvider {

    /**
     *
     * Initialize per node transaction.
     *
     * @param nodeId IID for particular node
     * @return per node tx
     * @throws TxException.TxInitiatizationFailedException thrown when unable to initialize the tx
     */
    ReadWriteTransaction newTx(InstanceIdentifier<?> nodeId)
        throws TxException.TxInitiatizationFailedException;

}
