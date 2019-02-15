import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {UserSelection} from '../user-selection/userSelectionModels';
import {MeterDefinition, Medium, Quantity} from './meter-definitions/meterDefinitionModels';
import {MeterDetails} from './meter-details/meterDetailsModels';
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
  entities: ObjectsById<T>;
  result: uuid[];
}

export interface NormalizedState<T extends Identifiable> extends DomainModel<T> {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  total: number;
  error?: ErrorResponse;
}

export interface DomainModelsState {
  gatewayMapMarkers: NormalizedState<MapMarker>;
  meterMapMarkers: NormalizedState<MapMarker>;
  meters: NormalizedState<MeterDetails>;
  organisations: NormalizedState<Organisation>;
  userSelections: NormalizedState<UserSelection>;
  users: UserState;
  meterDefinitions: NormalizedState<MeterDefinition>;
  mediums: NormalizedState<Medium>;
  quantities: NormalizedState<Quantity>;
}

export const enum RequestType {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  GET_ENTITIES = 'GET_ENTITIES',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}
