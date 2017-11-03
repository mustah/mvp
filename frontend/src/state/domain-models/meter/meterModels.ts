import {uuid} from '../../../types/Types';
import {MappedObject} from '../domainModels';

export interface Meter extends MappedObject {
  id: string;
  facility: string;
  medium: string;
  manufacturer: string;
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
