import {uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';

export interface Gateway extends MapMarker {
  id: string;
  facility: string;
  productModel: string;
  telephoneNo: string;
  ip: string | null;
  port: string | null;
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
