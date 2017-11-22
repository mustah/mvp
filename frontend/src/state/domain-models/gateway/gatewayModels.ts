import {MapMarker} from '../../../usecases/map/mapModels';
import {NormalizedState} from '../domainModels';
import {Flag} from '../flag/flagModels';
import {IdNamed} from '../../../types/Types';

export interface Gateway extends MapMarker {
  id: string;
  facility: string;
  flags: Flag[];
  productModel: string;
  telephoneNo: string;
  statusChanged?: string;
  ip: string | null;
  port: string | null;
  meterIds: string[];
  meterStatus: IdNamed;
  alarm: string;
}

export type GatewaysState = NormalizedState<Gateway>;
