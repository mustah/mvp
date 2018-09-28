import {Identifiable, uuid} from '../../../types/Types';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface MeterStatusChangelog extends Identifiable {
  name: string;
  start: string;
}

export interface Alarm {
  id: uuid;
  mask: number;
  description?: string;
}

export type MetersState = NormalizedPaginatedState<Meter>;

export interface Meter extends Identifiable, LocationHolder {
  alarm?: Alarm;
  address?: string;
  collectionPercentage?: number;
  readIntervalMinutes?: number;
  facility: uuid;
  isReported?: boolean;
  medium: string; // TODO type as Medium
  manufacturer: string;
  statusChanged?: string;
  gatewaySerial: string;
  organisationId: uuid;
}
