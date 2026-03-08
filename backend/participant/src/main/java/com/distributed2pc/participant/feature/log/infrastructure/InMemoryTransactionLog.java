package com.distributed2pc.participant.feature.log.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.log.domain.LogEntry;

/**
 * Thread-safe in-memory transaction log for a participant.
 *
 * <p>
 * In a production fault-tolerant implementation this would be backed by
 * a durable store (file, database). For simulation purposes an in-memory map
 * suffices.
 */
@Repository
public class InMemoryTransactionLog {

    private final Map<UUID, LogEntry> log = new ConcurrentHashMap<>();

    /**
     * Writes or overwrites the log entry for the given transaction.
     *
     * @param entry the entry to persist.
     */
    public void write(LogEntry entry) {
        log.put(entry.transactionId(), entry);
    }

    /**
     * Retrieves the last known log entry for a transaction.
     *
     * @param transactionId the transaction to look up.
     * @return the entry wrapped in Optional, or empty if not found.
     */
    public Optional<LogEntry> read(UUID transactionId) {
        return Optional.ofNullable(log.get(transactionId));
    }

    /**
     * Updates the phase of an existing log entry.
     *
     * @param transactionId the transaction whose phase is being updated.
     * @param phase         the new phase.
     */
    public void updatePhase(UUID transactionId, TransactionStatus phase) {
        log.computeIfPresent(transactionId, (id, existing) -> existing.withPhase(phase));
    }

    /**
     * Returns all log entries whose phase matches the given status.
     *
     * @param phase the phase to filter by.
     * @return snapshot list of matching entries.
     */
    public List<LogEntry> findAllWithPhase(TransactionStatus phase) {
        return log.values().stream()
                .filter(e -> e.phase() == phase)
                .toList();
    }

    /**
     * Returns the total number of entries in the log.
     *
     * @return entry count.
     */
    public int size() {
        return log.size();
    }

    /**
     * Returns the value from the most recently committed transaction, or empty if
     * no committed transaction has been recorded yet.
     *
     * @return the latest committed value wrapped in an Optional.
     */
    public Optional<String> getLatestCommittedValue() {
        return log.values().stream()
                .filter(e -> e.phase() == TransactionStatus.COMMITTED)
                .max(Comparator.comparing(LogEntry::timestamp))
                .map(LogEntry::value);
    }
}
