import {uuid} from '../../../types/Types';

export interface Meter {
  id: string;
  facility: string;
  address: string;
  city: string;
  medium: string;
  manufacturer: string;
  status: string;
  gatewayId: string;
  position: string;
}

export interface Meters {
  result: uuid[];
  entities: {
    meters: {[key: string]: Meter};
  };
}

export interface MetersState extends Meters {
  isFetching: boolean;
  total: number;
}
