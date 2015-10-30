package org.opendaylight.distributed.tx.impl.spi;

import com.google.common.base.Preconditions;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.distributed.tx.api.DTx;
import org.opendaylight.distributed.tx.api.DTxException;
import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DTxProviderImpl implements DTxProvider, AutoCloseable, BindingAwareConsumer {

    private MountPointService mountPointService;

    @Override public synchronized DTx newTx(final Set<InstanceIdentifier<?>> nodes)
        throws DTxException.DTxInitializationFailedException {
        Preconditions.checkNotNull(mountPointService);
        return null;
    }

    @Override public void close() throws Exception {

    }

    @Override public void onSessionInitialized(final BindingAwareBroker.ConsumerContext consumerContext) {
        mountPointService = consumerContext.getSALService(MountPointService.class);
    }
}
