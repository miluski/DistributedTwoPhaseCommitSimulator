import type { ParticipantInfo } from '@common/types';
import type { ScenarioCategory } from './ScenarioCategory';
import type { ScenarioResult } from './ScenarioResult';

/** A runnable fault scenario with structured metadata. */
export interface Scenario {
  id: string;
  label: string;
  category: ScenarioCategory;
  description: string;
  expectedBehaviour: string;
  run: (participants: readonly ParticipantInfo[]) => Promise<ScenarioResult>;
}
