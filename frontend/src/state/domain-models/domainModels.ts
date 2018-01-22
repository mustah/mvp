import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {GatewaysState} from './gateway/gatewayModels';
import {MeasurementState} from './measurement/measurementModels';
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

interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

export interface PaginatedResult {
  content: uuid[];
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  sort: SortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface NormalizedPaginated<T> {
  entities: DomainModel<T>;
  result: PaginatedResult;
}

export interface NormalizedPaginatedState<T> extends NormalizedPaginated<T> {
  error?: ErrorResponse;
  isFetching: boolean;
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
  measurements: MeasurementState;
  meterStatuses: SelectionEntityState;
  meters: MetersState;
  productModels: SelectionEntityState;
  users: UserState;
}

export enum HttpMethod {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}
