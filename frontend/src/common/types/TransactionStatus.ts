/** Possible 2PC transaction lifecycle states matching the backend enum. */
export type TransactionStatus = 'INITIATED' | 'PREPARING' | 'COMMITTED' | 'ABORTED' | 'UNCERTAIN';
