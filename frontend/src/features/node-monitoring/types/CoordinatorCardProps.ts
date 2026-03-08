import type { CoordinatorStatusResponse } from '@common/types';

/** Props for the {@link CoordinatorCard} component. */
export interface CoordinatorCardProps {
  coordinatorStatus: CoordinatorStatusResponse | null;
}
