package org.opendaylight.distributed.tx.it.provider;

import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.DistributedTxItModelService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.NaiveTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.NaiveTestOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.Future;

public class DistributedTxProviderImpl implements DistributedTxItModelService {
    DTxProvider dTxProvider;

    @Override
    public Future<RpcResult<NaiveTestOutput>> naiveTest(NaiveTestInput input) {
        return null;
    }

    public DistributedTxProviderImpl(DTxProvider provider){
        this.dTxProvider = provider;
    }
}
