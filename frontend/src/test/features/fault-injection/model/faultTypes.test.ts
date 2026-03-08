import { COORDINATOR_FAULT_TYPES, FAULT_TYPES } from '@features/fault-injection/model/faultTypes';
import { describe, expect, it } from 'vitest';

describe('FAULT_TYPES', () => {
  it('givenExport_hasSixFaultTypes', () => {
    expect(FAULT_TYPES).toHaveLength(6);
  });

  it.each([
    ['CRASH'],
    ['NETWORK_DELAY'],
    ['FORCE_ABORT_VOTE'],
    ['MESSAGE_LOSS'],
    ['TRANSIENT'],
    ['INTERMITTENT'],
  ] as const)('givenFaultType_%s_isPresentInList', (faultType) => {
    expect(FAULT_TYPES).toContain(faultType);
  });

  it('givenExport_firstTypeIsCrash', () => {
    expect(FAULT_TYPES[0]).toBe('CRASH');
  });
});

describe('COORDINATOR_FAULT_TYPES', () => {
  it('givenExport_hasThreeFaultTypes', () => {
    expect(COORDINATOR_FAULT_TYPES).toHaveLength(3);
  });

  it.each([['CRASH'], ['NETWORK_DELAY'], ['MESSAGE_LOSS']] as const)(
    'givenFaultType_%s_isPresentInCoordinatorList',
    (faultType) => {
      expect(COORDINATOR_FAULT_TYPES).toContain(faultType);
    }
  );

  it('givenParticipantOnlyTypes_areNotInCoordinatorList', () => {
    expect(COORDINATOR_FAULT_TYPES).not.toContain('FORCE_ABORT_VOTE');
    expect(COORDINATOR_FAULT_TYPES).not.toContain('TRANSIENT');
    expect(COORDINATOR_FAULT_TYPES).not.toContain('INTERMITTENT');
  });
});
