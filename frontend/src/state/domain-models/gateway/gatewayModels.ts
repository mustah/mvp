import {uuid} from '../../../types/Types';

export interface Gateway {
    id: string;
    facility: string;
    address: string;
    city: string;
    productModel: string;
    telephoneNo: string;
    ip: string;
    port: string;
    status: string;
    position: string;
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
