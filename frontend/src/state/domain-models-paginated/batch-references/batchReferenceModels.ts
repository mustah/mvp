import {Identifiable} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface BatchReference extends Identifiable {
  created: string;
  requireApproval: boolean;
  status: string;
}

export type BatchReferencesState = NormalizedPaginatedState<BatchReference>;
