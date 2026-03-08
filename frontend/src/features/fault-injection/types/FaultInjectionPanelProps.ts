import type { ParticipantInfo } from '@common/types';

/** Props for the {@link FaultInjectionPanel} component. */
export interface FaultInjectionPanelProps {
  /** List of registered participant nodes available for fault targeting. */
  readonly participants: readonly ParticipantInfo[];
}
