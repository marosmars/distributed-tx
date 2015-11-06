/*
 * Copyright and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.distributed.tx.impl;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.distributed.tx.impl.spi.DTxProviderImpl;

public class DistributedTxProviderTest {
    @Test
    public void testOnSessionInitiated() {
        DTxProviderImpl provider = new DTxProviderImpl(txProvider);

        // ensure no exceptions
        // currently this method is empty
        provider.onSessionInitialized(mock(BindingAwareBroker.ProviderContext.class));
    }

    @Test
    public void testClose() throws Exception {
        DTxProviderImpl provider = new DTxProviderImpl(txProvider);

        // ensure no exceptions
        // currently this method is empty
        provider.close();
    }
}
