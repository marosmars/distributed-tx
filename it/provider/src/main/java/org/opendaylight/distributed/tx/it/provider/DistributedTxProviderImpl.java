package org.opendaylight.distributed.tx.it.provider;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.distributed.tx.api.DTxProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.DistributedTxItModelService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.NaiveTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.NaiveTestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.distributed.tx.it.model.rev150105.NaiveTestOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.concurrent.Future;

public class DistributedTxProviderImpl implements DistributedTxItModelService {
    DTxProvider dTxProvider;

    @Override
    public Future<RpcResult<NaiveTestOutput>> naiveTest(NaiveTestInput input) {
        NaiveTestOutput output = new NaiveTestOutputBuilder().setResult("Bingo").build();
        return Futures.immediateFuture(RpcResultBuilder.success(output).build());
    }

    public DistributedTxProviderImpl(DTxProvider provider){
        this.dTxProvider = provider;
        this.dTxProvider.test();
    }
}
