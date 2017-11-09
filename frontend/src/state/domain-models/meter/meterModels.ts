import {uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';

export interface Meter extends MapMarker {
  id: string;
  facility: string;
  medium: string;
  manufacturer: string;
  gatewayId: string;
  statusChanged: string;
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
