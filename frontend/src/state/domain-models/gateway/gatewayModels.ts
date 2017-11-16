import {uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';
import {Flag} from '../flag/flagModels';

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

export interface Gateways {
  result: uuid[];
  entities: {
    gateways: {[key: string]: Gateway};
  };
}

export interface GatewaysState extends Gateways {
  isFetching: boolean;
  total: number;
}
