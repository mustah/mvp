import {ErrorResponse, IdNamed, uuid} from '../../../types/Types';
import {Address} from '../../../state/domain-models/domainModels';

export interface DomainModel<T> {
  [key: string]: T;
}

export interface Normalized<T> {
  result: uuid[];
  entities: DomainModel<T>;
}

export interface NormalizedState<T> extends Normalized<T> {
  isFetching: boolean;
  total: number;
  error?: ErrorResponse;
}

export type IdNamedState = NormalizedState<IdNamed>;
export type AddressState = NormalizedState<Address>;

export interface GeoDataState {
  cities: IdNamedState;
  addresses: AddressState;
  sidebarTree: any;
}
