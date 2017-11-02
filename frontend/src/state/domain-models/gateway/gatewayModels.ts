import {uuid} from '../../../types/Types';
import {Location} from '../domainModels';

export interface Gateway extends Location {
  id: string;
  facility: string;
  productModel: string;
  telephoneNo: string;
  ip: string | null;
  port: string | null;
  status: string;
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
