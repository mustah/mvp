import {HasId, IdNamed, uuid} from '../../../types/Types';
import {Location} from '../../domain-models/domainModels';
import {Flag} from '../../domain-models/flag/flagModels';

export interface MeterStatusChangelog {
  id: uuid;
  statusId: uuid;
  name: string;
  start: string;
}

export interface Meter extends Location, HasId {
  moid: uuid;
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
