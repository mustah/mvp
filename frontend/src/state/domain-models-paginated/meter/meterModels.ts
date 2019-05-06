import {Identifiable, uuid} from '../../../types/Types';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export const enum EventLogType {
  newMeter = 'newMeter',
  statusChange = 'statusChange',
}

export interface EventLog extends Identifiable {
  type: EventLogType;
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
  alarms?: Alarm[];
  address?: string;
  readIntervalMinutes?: number;
  facility: uuid;
  isReported?: boolean;
  medium: string; // TODO type as Medium
  manufacturer: string;
  statusChanged?: string;
  gatewaySerial: string;
  organisationId: uuid;
  mbusDeviceType?: number;
  revision?: number;
}
