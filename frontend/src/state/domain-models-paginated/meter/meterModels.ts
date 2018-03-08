import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
import {GatewayMandatory} from '../../domain-models/gateway/gatewayModels';
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
  gateway: GatewayMandatory;
}
