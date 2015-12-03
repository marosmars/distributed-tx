package org.opendaylight.distributed.tx.impl.spi;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.distributed.tx.api.DTx;
import org.opendaylight.distributed.tx.api.DTxException;
import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.distributed.tx.spi.TxProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.logging.Logger;

public class DTXProviderService implements DTxProvider, AutoCloseable, BindingAwareConsumer{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DTXProviderService.class);
    DTxProviderImpl dtxProvider;
    TxProvider mountServiceProvider;

    public DTXProviderService(TxProvider msProvider) {
        this.mountServiceProvider = msProvider;
        this.dtxProvider = new DTxProviderImpl(this.mountServiceProvider);
    }

    @Nonnull
    @Override
    public DTx newTx(@Nonnull Set<InstanceIdentifier<?>> nodes) throws DTxException.DTxInitializationFailedException {
        return this.dtxProvider.newTx(nodes);
    }

    @Override
    public void close() throws Exception {
        this.dtxProvider = null;
        this.mountServiceProvider= null;
    }

    @Override
    public void onSessionInitialized(BindingAwareBroker.ConsumerContext session) {
        LOG.info("DTXPrividerService started.");
    }
}
