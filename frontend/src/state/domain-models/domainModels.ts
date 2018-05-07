import {ErrorResponse, Identifiable, IdNamed, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {UserSelection} from '../user-selection/userSelectionModels';
import {Address} from './location/locationModels';
import {Organisation} from './organisation/organisationModels';
import {UserState} from './user/userModels';

export interface ObjectsById<T extends Identifiable> {
  [id: string]: T;
}

export interface Normalized<T extends Identifiable> {
  result: uuid[];
  entities: {
    [entityType: string]: ObjectsById<T>,
  };
}

export interface DomainModel<T extends Identifiable> {
  result: uuid[];
  entities: ObjectsById<T>;
}

export interface NormalizedState<T extends Identifiable> extends DomainModel<T> {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  total: number;
  error?: ErrorResponse;
}

export type SelectionEntity = IdNamed | Address;

export type SelectionEntityState = NormalizedState<SelectionEntity>;

export interface DomainModelsState {
  addresses: SelectionEntityState;
  alarms: SelectionEntityState;
  cities: SelectionEntityState;
  countries: SelectionEntityState;
  gatewayMapMarkers: NormalizedState<MapMarker>;
  gatewayStatuses: SelectionEntityState;
  media: NormalizedState<IdNamed>;
  meterMapMarkers: NormalizedState<MapMarker>;
  meterStatuses: SelectionEntityState;
  organisations: NormalizedState<Organisation>;
  userSelections: NormalizedState<UserSelection>;
  users: UserState;
}

export const enum RequestType {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}
