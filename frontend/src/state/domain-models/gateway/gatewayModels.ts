import {IdNamed, uuid} from '../../../types/Types';
import {NormalizedState, Location} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface GatewayStatusChangelog {
  id: uuid;
  gatewayId: uuid;
  status: IdNamed;
  date: string;
}

export interface Gateway extends Location {
  id: uuid;
  facility: string;
  flags: Flag[];
  productModel: string;
  telephoneNumber: string;
  statusChanged?: string;
  ip: string | null;
  port: string | null;
  signalToNoiseRatio?: number;
  status: IdNamed;
  statusChangelog: GatewayStatusChangelog[];
  meterIds: uuid[];
  meterStatus: IdNamed;
  meterAlarm: string;
  meterManufacturer: string;
}

export type GatewaysState = NormalizedState<Gateway>;
