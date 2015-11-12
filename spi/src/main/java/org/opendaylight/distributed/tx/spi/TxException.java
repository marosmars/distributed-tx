package org.opendaylight.distributed.tx.spi;

/**
 * Per Node transaction failure
 */
public class TxException extends RuntimeException {

    public TxException(final String s, final RuntimeException e) {
        super(s, e);
    }

    public TxException(final String s) {
        super(s);
    }

    /**
     * Generic per-node-tx initialization failure
     */
    public static class TxInitiatizationFailedException extends TxException {

        public TxInitiatizationFailedException(final String s, final RuntimeException e) {
            super(s, e);
        }

        public TxInitiatizationFailedException(final String s) {
            super(s);
        }
    }

    /**
     * Thrown when the per-node-tx-provider cant find the node and thus cant event attempt to create a Tx for it
     */
    public static class UnknownNodeException extends TxInitiatizationFailedException {

        public UnknownNodeException(final String s, final RuntimeException e) {
            super(s, e);
        }
    }

}
