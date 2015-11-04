package org.opendaylight.distributed.tx.spi;

import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;

public final class CachedData {

    public final InstanceIdentifier<?> id;
    public final DataObject data;
    public final ModifyAction operation;

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

}
