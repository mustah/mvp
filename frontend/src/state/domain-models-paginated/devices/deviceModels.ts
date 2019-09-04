import {Dictionary, Identifiable, uuid} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface Device extends Identifiable {
  organisationId: uuid;
  deviceType: string;
  claimStatus: string;
  attributes: Dictionary<string>;
}

export type DevicesState = NormalizedPaginatedState<Device>;
