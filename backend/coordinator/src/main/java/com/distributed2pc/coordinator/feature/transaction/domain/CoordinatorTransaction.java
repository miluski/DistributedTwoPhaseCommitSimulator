package com.distributed2pc.coordinator.feature.transaction.domain;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable coordinator-side view of a 2PC transaction.
 *
 * <p>Fields that require concurrent write access use volatile or
 * thread-safe structures. Callers must coordinate Phase 1 and Phase 2
 * transitions externally (e.g., via a CompletableFuture chain) to avoid races.
 */
public class CoordinatorTransaction {

 private final UUID id;
 private final String value;
 private final Instant initiatedAt;
 private volatile TransactionStatus status;
 private volatile Instant decidedAt;
 private final Map<String, VoteResult> votes = new ConcurrentHashMap<>();

 /**
  * Creates a transaction in {@link TransactionStatus#INITIATED} state.
  *
  * @param value the payload to be committed across all participants.
  */
 public CoordinatorTransaction(String value) {
  this.id = UUID.randomUUID();
  this.value = value;
  this.initiatedAt = Instant.now();
  this.status = TransactionStatus.INITIATED;
 }

 /**
  * Records a vote from one participant.
  *
  * @param serverId the participant that cast the vote.
  * @param vote     the vote result.
  */
 public void recordVote(String serverId, VoteResult vote) {
  votes.put(serverId, vote);
 }

 /**
  * Applies the final COMMIT or ABORT decision and stamps the decision time.
  *
  * @param decision must be {@link TransactionStatus#COMMITTED} or {@link TransactionStatus#ABORTED}.
  */
 public void applyDecision(TransactionStatus decision) {
  this.status = decision;
  this.decidedAt = Instant.now();
 }

 /** @return the unique transaction identifier. */
 public UUID getId() { return id; }

 /** @return the payload value that was submitted for commit. */
 public String getValue() { return value; }

 /** @return the timestamp when this transaction was created. */
 public Instant getInitiatedAt() { return initiatedAt; }

 /** @return the current lifecycle status of this transaction. */
 public TransactionStatus getStatus() { return status; }

 /**
  * Directly sets the transaction status without recording a decision timestamp.
  *
  * @param status the new status to set.
  */
 public void setStatus(TransactionStatus status) { this.status = status; }

 /** @return the timestamp when the COMMIT or ABORT decision was made, or {@code null} if pending. */
 public Instant getDecidedAt() { return decidedAt; }

 /**
  * Returns an immutable snapshot of votes collected during Phase 1.
  *
  * @return map of serverId to vote result.
  */
 public Map<String, VoteResult> getVotes() { return Map.copyOf(votes); }
}
