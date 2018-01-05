import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';
import {UserState} from './user/userModels';

export interface Location {
  address: Address;
  city: IdNamed;
  position: GeoPosition;
}

export const enum EndPoints {
  selections = '/selections',
  meters = '/meters',
  gateways = '/gateways',
  users = '/users',
  authenticate = '/authenticate',
}

export interface GeoPosition {
  latitude: number;
  longitude: number;
  confidence: number;
}

export interface Address extends IdNamed {
  cityId: uuid;
}

export interface DomainModel<T> {
  [id: string]: T;
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

export type SelectionEntityState = NormalizedState<SelectionEntity>;

export type SelectionEntity = IdNamed | Address;

export interface DomainModelsState {
  addresses: SelectionEntityState;
  cities: SelectionEntityState;
  alarms: SelectionEntityState;
  manufacturers: SelectionEntityState;
  productModels: SelectionEntityState;
  meterStatuses: SelectionEntityState;
  gatewayStatuses: SelectionEntityState;
  gateways: GatewaysState;
  meters: MetersState;
  users: UserState;
}
