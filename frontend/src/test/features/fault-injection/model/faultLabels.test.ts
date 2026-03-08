import { FAULT_LABELS } from '@features/fault-injection/model/faultLabels';
import type { FaultType } from '@features/fault-injection/types';
import { describe, expect, it } from 'vitest';

const ALL_FAULT_TYPES: FaultType[] = [
  'CRASH',
  'NETWORK_DELAY',
  'FORCE_ABORT_VOTE',
  'MESSAGE_LOSS',
  'TRANSIENT',
  'INTERMITTENT',
];

describe('FAULT_LABELS', () => {
  it('givenExport_hasSixEntries', () => {
    expect(Object.keys(FAULT_LABELS)).toHaveLength(6);
  });

  it.each(ALL_FAULT_TYPES)('givenFaultType_%s_hasNonEmptyPolishLabel', (faultType) => {
    expect(typeof FAULT_LABELS[faultType]).toBe('string');
    expect(FAULT_LABELS[faultType].length).toBeGreaterThan(0);
  });

  it('givenCrash_labelContainsCrash', () => {
    expect(FAULT_LABELS['CRASH']).toContain('CRASH');
  });

  it('givenNetworkDelay_labelMentionsDelay', () => {
    expect(FAULT_LABELS['NETWORK_DELAY'].toLowerCase()).toContain('opóźnienie');
  });
});
