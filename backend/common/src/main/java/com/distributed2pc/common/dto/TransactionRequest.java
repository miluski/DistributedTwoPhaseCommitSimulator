package com.distributed2pc.common.dto;

import java.util.UUID;

/**
 * Request body for initiating a new 2PC transaction via the coordinator REST
 * API.
 *
 * @param value Arbitrary string payload to be committed across all
 *              participants.
 */
public record TransactionRequest(String value) {

  /**
   * Factory method for convenient test construction.
   *
   * @return A request with a random value.
   */
  public static TransactionRequest random() {
    return new TransactionRequest("value-" + UUID.randomUUID());
  }
}
