package org.opendaylight.distributed.tx.spi;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * APIs for the Rollback functionality, setting the data for all nodes to pre-distributedTx state.
 */
public interface Rollback {

    ListenableFuture<Void> rollback(@Nonnull Map<InstanceIdentifier<?>, ? extends TxCache> nodeCache,
        @Nonnull Map<InstanceIdentifier<?>, ? extends ReadWriteTransaction> nodeTx);

}
