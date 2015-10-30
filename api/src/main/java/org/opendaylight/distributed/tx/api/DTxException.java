package org.opendaylight.distributed.tx.api;

public class DTxException extends RuntimeException {

    /**
     * Distributed tx initialization failure.
     */
    public static class DTxInitializationFailedException extends DTxException {

    }

    /**
     * Edit operation failure for one or more devices.
     */
    public static class EditFailedException extends DTxException {

    }

    /**
     * Rollback failure for distributed tx. This indicates unknown resulting state.
     */
    public static class RollbackFailedException extends DTxException {

    }

    /**
     * Submit operation failure for one or more devices.
     */
    public static class SubmitFailedException extends DTxException {
    }
}
