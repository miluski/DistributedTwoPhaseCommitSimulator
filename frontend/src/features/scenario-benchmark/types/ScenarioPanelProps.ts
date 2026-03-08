import type { ParticipantInfo } from '@common/types';

/** Props for the {@link ScenarioPanel} component. */
export interface ScenarioPanelProps {
  /** List of registered participant nodes. */
  readonly participants: readonly ParticipantInfo[];
}
