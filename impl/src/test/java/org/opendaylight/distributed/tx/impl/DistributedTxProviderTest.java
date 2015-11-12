/*
 * Copyright and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.distributed.tx.impl;

import org.junit.Test;
import org.opendaylight.distributed.tx.impl.spi.DTxProviderImpl;
import org.opendaylight.distributed.tx.spi.TxProvider;

public class DistributedTxProviderTest {
    private TxProvider txProvider;

    public void addProvider(final TxProvider txProvider) {
        this.txProvider = txProvider;
    }
    @Test
    public void testOnSessionInitiated() {
        DTxProviderImpl provider = new DTxProviderImpl(txProvider);
    }

    @Test
    public void testClose() throws Exception {
        DTxProviderImpl provider = new DTxProviderImpl(txProvider);

        // ensure no exceptions
        // currently this method is empty
        provider.close();
    }
}
