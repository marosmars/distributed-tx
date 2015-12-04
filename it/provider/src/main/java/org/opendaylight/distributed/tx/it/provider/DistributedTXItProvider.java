package org.opendaylight.distributed.tx.it.provider;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.DistributedTxItModelService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DistributedTXItProvider implements BindingAwareProvider, AutoCloseable {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(DistributedTXItProvider.class);
    private BindingAwareBroker.RpcRegistration<DistributedTxItModelService> dtxItModelService;
    DTxProvider dTxProvider;

    public  DistributedTXItProvider(DTxProvider provider){
        this.dTxProvider = provider;
    }
    @Override
    public void close() throws Exception {
        this.dtxItModelService = null;
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext session) {
        this.dtxItModelService = session.addRpcImplementation(DistributedTxItModelService.class, new DistributedTxProviderImpl(this.dTxProvider));
        log.info("Service Distributed tx IT provider statred");
    }
}

