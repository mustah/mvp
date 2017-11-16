import {MapMarker} from '../../../usecases/map/mapModels';
import {Flag} from '../flag/flagModels';
import {NormalizedState} from '../domainModels';

export interface Gateway extends MapMarker {
  id: string;
  facility: string;
  flags: Flag[];
  productModel: string;
  telephoneNo: string;
  ip: string | null;
  port: string | null;
  meterIds: string[];
}

export type GatewaysState = NormalizedState<Gateway>;
