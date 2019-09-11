import {Identifiable, uuid} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface DeviceResponseDto {
  deviceEui: string;
}

export interface BatchReference extends Identifiable {
  created: string;
  requireApproval: boolean;
  status: string;
}

export interface BatchRequestState {
  batchId: string;
  deviceEuis: string[];
  deviceEuisText: string;
  organisationId: uuid;
  requireApproval: boolean;
}

export type BatchReferencesState = NormalizedPaginatedState<BatchReference>;
