import {ErrorResponse, uuid} from '../../../types/Types';

export interface DomainModel {
  [key: string]: any;
}

export interface Normalized {
  result: uuid[];
  entities: DomainModel;
}

export interface NormalizedState extends Normalized {
  isFetching: boolean;
  total: number;
  error?: ErrorResponse;
}

export interface GeoDataState {
  cities: NormalizedState;
  addresses: NormalizedState;
}
