package org.opendaylight.distributed.tx.impl.spi;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.distributed.tx.spi.CachedData;
import org.opendaylight.distributed.tx.spi.Rollback;
import org.opendaylight.distributed.tx.spi.TxCache;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;

public class RollbackImpl implements Rollback {

    @Override public ListenableFuture<Void> rollback(
        @Nonnull final Map<InstanceIdentifier<?>, ? extends TxCache> nodeCache,
        @Nonnull final Map<InstanceIdentifier<?>, ? extends ReadWriteTransaction> nodeTx) {

        for (Map.Entry<InstanceIdentifier<?>, ? extends TxCache> instanceIdentifierEntry : nodeCache.entrySet()) {
            InstanceIdentifier<?> nodeId = instanceIdentifierEntry.getKey();
            TxCache perNodeCache = instanceIdentifierEntry.getValue();

            for (CachedData cachedData : perNodeCache) {
                final ModifyAction operation = cachedData.getOperation();
                final Optional<DataObject> data = cachedData.getData();

                ModifyAction revertAction = getRevertAction(operation, data);



            }
        }

        // TODO
        return null;

    }

    private ModifyAction getRevertAction(final ModifyAction operation, final Optional<DataObject> data) {
        switch (operation) {
        case MERGE: {
            return data.isPresent() ? ModifyAction.REPLACE : ModifyAction.DELETE;
        }
        case REPLACE: {
            return data.isPresent() ? ModifyAction.REPLACE : ModifyAction.DELETE;
        }
        case DELETE:
            return data.isPresent() ? ModifyAction.REPLACE : ModifyAction.NONE;
        }

//        default {
            throw new IllegalStateException("Unexpected operation: " + operation);
//        }
    }
}
