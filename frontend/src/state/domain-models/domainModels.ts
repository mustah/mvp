import {ErrorResponse, IdNamed, uuid} from '../../types/Types';

export interface Location {
  address: Address;
  city: IdNamed;
  position: GeoPosition;
}

export enum EndPoints {
  selections = '/selections',
  meters = '/meters',
  gateways = '/gateways',
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

export type SelectionEntityState = NormalizedState<SelectionEntity>;
// TODO: Find a way to include gateways and meters in SelectionEntity -> Rename DomainModelEntity
export type SelectionEntity = IdNamed | Address;
