package org.opendaylight.distributed.tx.spi;

import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;

// FIXME javadoc needed for classes and methods (also for other SPI classes here)
public final class CachedData {

    private final InstanceIdentifier<?> id;
    private final DataObject data;
    private final ModifyAction operation;

    public CachedData(@Nonnull final InstanceIdentifier<?> id,
        @Nullable final DataObject data, @Nonnull final ModifyAction operation) {
        this.id = id;
        this.data = data;
        this.operation = operation;
    }

    public Optional<DataObject> getData() {
        return Optional.fromNullable(data);
    }

    public InstanceIdentifier<?> getId() {
        return id;
    }

    public ModifyAction getOperation() {
        return operation;
    }

    // FIXME !! store the ds type here, dont return fixed CONFIGURATION. Netconf might only support CONFIG writes,
    // but other mountpoints might also support writes to operational
    public LogicalDatastoreType getDsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
