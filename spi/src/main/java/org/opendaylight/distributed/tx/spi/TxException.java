package org.opendaylight.distributed.tx.spi;

/**
 * Per Node transaction failure
 */
public class TxException extends RuntimeException {

    /**
     * Generic per-node-tx initialization failure
     */
    public static class TxInitiatizationFailedException extends TxException {

    }

    /**
     * Thrown when the per-node-tx-provider cant find the node and thus cant event attempt to create a Tx for it
     */
    public static class UnknownNodeException extends TxInitiatizationFailedException {

    }

}
