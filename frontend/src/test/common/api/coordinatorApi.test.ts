import {
  fetchMetrics,
  fetchParticipants,
  fetchSimulationConfig,
  fetchTransactions,
  initiateTransaction,
  injectCoordinatorFault,
  injectFault,
  updateCoordinatorSimulationConfig,
  updateParticipantSimulationConfig,
} from '@common/api';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function okJson(body: unknown): Response {
  return { ok: true, status: 200, json: () => Promise.resolve(body) } as unknown as Response;
}

function errorResponse(status: number): Response {
  return { ok: false, status } as unknown as Response;
}

describe('coordinatorApi', () => {
  beforeEach(() => vi.clearAllMocks());
  afterEach(() => vi.restoreAllMocks());

  describe('fetchParticipants', () => {
    it('fetchParticipants_givenOkResponse_returnsParticipantList', async () => {
      const data = [{ serverId: 'p1' }];
      mockFetch.mockResolvedValue(okJson(data));

      const result = await fetchParticipants();

      expect(mockFetch).toHaveBeenCalledWith('/api/participants');
      expect(result).toEqual(data);
    });

    it('fetchParticipants_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(500));

      await expect(fetchParticipants()).rejects.toThrow('500');
    });
  });

  describe('fetchTransactions', () => {
    it('fetchTransactions_givenOkResponse_returnsTransactionList', async () => {
      const data = [{ transactionId: 'tx-1' }];
      mockFetch.mockResolvedValue(okJson(data));

      const result = await fetchTransactions();

      expect(mockFetch).toHaveBeenCalledWith('/api/transactions');
      expect(result).toEqual(data);
    });

    it('fetchTransactions_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(500));

      await expect(fetchTransactions()).rejects.toThrow('500');
    });
  });

  describe('initiateTransaction', () => {
    it('initiateTransaction_givenOkResponse_returnsTransactionResponse', async () => {
      const data = { transactionId: 'tx-1', status: 'COMMITTED' };
      mockFetch.mockResolvedValue(okJson(data));

      const result = await initiateTransaction('hello');

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/transactions',
        expect.objectContaining({ method: 'POST', body: JSON.stringify({ value: 'hello' }) })
      );
      expect(result).toEqual(data);
    });

    it('initiateTransaction_givenErrorResponse_throwsWithPolishMessage', async () => {
      mockFetch.mockResolvedValue(errorResponse(400));

      await expect(initiateTransaction('test')).rejects.toThrow(
        'Nieprawidłowe żądanie transakcji.'
      );
    });
  });

  describe('injectFault', () => {
    it('injectFault_givenOkResponse_resolves', async () => {
      mockFetch.mockResolvedValue({ ok: true } as Response);

      await expect(injectFault(9001, 'CRASH', true)).resolves.toBeUndefined();
    });

    it('injectFault_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(503));

      await expect(injectFault(9001, 'CRASH', true)).rejects.toThrow('503');
    });
  });

  describe('injectCoordinatorFault', () => {
    it('injectCoordinatorFault_givenOkResponse_resolves', async () => {
      mockFetch.mockResolvedValue({ ok: true } as Response);

      await expect(injectCoordinatorFault('CRASH', true)).resolves.toBeUndefined();
    });

    it('injectCoordinatorFault_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(503));

      await expect(injectCoordinatorFault('CRASH', true)).rejects.toThrow('503');
    });
  });

  describe('fetchMetrics', () => {
    it('fetchMetrics_givenOkResponse_returnsMetrics', async () => {
      const data = {
        total: 5,
        committed: 3,
        aborted: 1,
        uncertain: 1,
        inProgress: 0,
        avgDecisionMs: 100,
        redundancyEnabled: true,
      };
      mockFetch.mockResolvedValue(okJson(data));

      const result = await fetchMetrics();

      expect(mockFetch).toHaveBeenCalledWith('/api/metrics');
      expect(result).toEqual(data);
    });

    it('fetchMetrics_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(500));

      await expect(fetchMetrics()).rejects.toThrow('500');
    });
  });

  describe('fetchSimulationConfig', () => {
    it('fetchSimulationConfig_givenOkResponse_returnsConfig', async () => {
      const data = { redundancyEnabled: true };
      mockFetch.mockResolvedValue(okJson(data));

      const result = await fetchSimulationConfig();

      expect(mockFetch).toHaveBeenCalledWith('/api/simulation/config');
      expect(result).toEqual(data);
    });

    it('fetchSimulationConfig_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(503));

      await expect(fetchSimulationConfig()).rejects.toThrow('503');
    });
  });

  describe('updateCoordinatorSimulationConfig', () => {
    it('updateCoordinatorSimulationConfig_givenOkResponse_returnsUpdatedConfig', async () => {
      const data = { redundancyEnabled: false };
      mockFetch.mockResolvedValue(okJson(data));

      const result = await updateCoordinatorSimulationConfig(false);

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/simulation/config',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ redundancyEnabled: false }),
        })
      );
      expect(result).toEqual(data);
    });

    it('updateCoordinatorSimulationConfig_givenErrorResponse_throwsWithStatus', async () => {
      mockFetch.mockResolvedValue(errorResponse(500));

      await expect(updateCoordinatorSimulationConfig(true)).rejects.toThrow('500');
    });
  });

  describe('updateParticipantSimulationConfig', () => {
    it('updateParticipantSimulationConfig_givenOkResponse_callsParticipantPort', async () => {
      const data = { redundancyEnabled: false };
      mockFetch.mockResolvedValue(okJson(data));

      const result = await updateParticipantSimulationConfig(9001, false);

      expect(mockFetch).toHaveBeenCalledWith(
        '/participant-proxy/9001/api/simulation/config',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ redundancyEnabled: false }),
        })
      );
      expect(result).toEqual(data);
    });

    it('updateParticipantSimulationConfig_givenErrorResponse_throwsWithPort', async () => {
      mockFetch.mockResolvedValue(errorResponse(503));

      await expect(updateParticipantSimulationConfig(9002, true)).rejects.toThrow('9002');
    });
  });
});
