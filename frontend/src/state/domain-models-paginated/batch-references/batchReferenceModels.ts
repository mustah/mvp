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
  organisationId: uuid;
  deviceEuis: string[];
  requireApproval: boolean;
}

export type BatchReferencesState = NormalizedPaginatedState<BatchReference>;
