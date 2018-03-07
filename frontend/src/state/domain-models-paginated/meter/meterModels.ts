import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
import {LocationHolder} from '../../domain-models/location/locationModels';

export interface MeterStatusChangelog extends Identifiable {
  statusId: uuid;
  name: string;
  start: string;
}

export interface Meter extends Identifiable, LocationHolder {
  sapId?: uuid;
  measurementId?: uuid;
  facility: uuid;
  alarm: string;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  statusChanged?: string;
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  status: IdNamed;
  gatewayId: uuid;
  gatewayStatus: IdNamed;
  gatewaySerial: uuid;
  gatewayProductModel: string;
}

export const enum MeterStatus {
  ok = 0,
  alarm = 3,
  unknown = 4,
}
