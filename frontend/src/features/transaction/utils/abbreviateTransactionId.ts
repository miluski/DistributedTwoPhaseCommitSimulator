/**
 * Abbreviates a transaction or node ID for compact table display.
 *
 * @param id full identifier string.
 * @returns last 6 characters prefixed with an ellipsis when longer than 8 chars.
 */
export function abbreviateTransactionId(id: string): string {
  return id.length > 8 ? `…${id.slice(-6)}` : id;
}
