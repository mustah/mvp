import {IdNamed} from '../../../types/Types';
import {NormalizedState, Location} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface Gateway extends Location {
  id: string;
  facility: string;
  flags: Flag[];
  productModel: string;
  telephoneNo: string;
  statusChanged?: string;
  ip: string | null;
  port: string | null;
  status: IdNamed;
  meterIds: string[];
  meterStatus: IdNamed;
  meterAlarm: string;
  meterManufacturer: string;
}

export type GatewaysState = NormalizedState<Gateway>;
