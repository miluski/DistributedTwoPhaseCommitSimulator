/** Props for the {@link TransactionPanel} component. */
export interface TransactionPanelProps {
  /** Optional callback invoked with the transaction ID when the user clicks "View Timeline". */
  readonly onTransactionSelect?: (txId: string) => void;
}
