import { NODE_STATUS_COLOR } from '@features/node-monitoring/model/nodeStatusStyles';
import { describe, expect, it } from 'vitest';

describe('NODE_STATUS_COLOR', () => {
  it('givenOnlineStatus_hasEmeraldClasses', () => {
    expect(NODE_STATUS_COLOR['ONLINE']).toContain('emerald');
  });

  it('givenCrashedStatus_hasRoseClasses', () => {
    expect(NODE_STATUS_COLOR['CRASHED']).toContain('rose');
  });

  it('givenDegradedStatus_hasAmberClasses', () => {
    expect(NODE_STATUS_COLOR['DEGRADED']).toContain('amber');
  });

  it('givenExport_hasExactlyThreeStatuses', () => {
    expect(Object.keys(NODE_STATUS_COLOR)).toHaveLength(3);
  });
});
