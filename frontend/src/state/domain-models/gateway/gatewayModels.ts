import {uuid} from '../../../types/Types';
import {MappedObject} from '../domainModels';

export interface Gateway extends MappedObject {
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
