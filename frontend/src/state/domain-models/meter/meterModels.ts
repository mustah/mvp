import {uuid} from '../../../types/Types';
import {Location} from '../domainModels';

export interface Meter extends Location {
  id: string;
  facility: string;
  medium: string;
  manufacturer: string;
  status: string;
  gatewayId: string;
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
