import {HasId, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
import {LocationHolder} from '../../domain-models/location/locationModels';

export interface MeterStatusChangelog extends HasId {
  statusId: uuid;
  name: string;
  start: string;
}

export interface Meter extends HasId, LocationHolder {
  sapId?: uuid;
  measurementId?: uuid;
  facility: string;
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
