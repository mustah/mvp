import {ErrorResponse, HasId, IdNamed, uuid} from '../../types/Types';
import {GatewaysState} from './gateway/gatewayModels';
import {MeasurementState} from './measurement/measurementModels';
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
  measurements = '/measurements',
}

export interface GeoPosition {
  latitude: number;
  longitude: number;
  confidence: number;
}

export interface Address extends IdNamed {
  cityId: uuid;
}

export interface ObjectsById<T extends HasId> {
  [id: string]: T;
}

export interface Normalized<T extends HasId> {
  result: uuid[];
  entities: {
    [entityType: string]: ObjectsById<T>,
  };
}

export interface DomainModel<T extends HasId> {
  result: uuid[];
  entities: ObjectsById<T>;
}

export interface NormalizedState<T extends HasId> extends DomainModel<T> {
  isFetching: boolean;
  total: number;
  error?: ErrorResponse;
}

export type SelectionEntity = IdNamed | Address;

export type SelectionEntityState = NormalizedState<SelectionEntity>;

export interface DomainModelsState {
  addresses: SelectionEntityState;
  alarms: SelectionEntityState;
  cities: SelectionEntityState;
  gatewayStatuses: SelectionEntityState;
  gateways: GatewaysState;
  manufacturers: SelectionEntityState;
  meterStatuses: SelectionEntityState;
  productModels: SelectionEntityState;
  measurements: MeasurementState;
  users: UserState;
}

export enum HttpMethod {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}
