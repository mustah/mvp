import {ErrorResponse, Identifiable, IdNamed, uuid} from '../../types/Types';
import {Meter} from '../domain-models-paginated/meter/meterModels';
import {GatewaysState} from './gateway/gatewayModels';
import {Address} from './location/locationModels';
import {MeasurementState} from './measurement/measurementModels';
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
  countries: SelectionEntityState;
  cities: SelectionEntityState;
  addresses: SelectionEntityState;
  alarms: SelectionEntityState;
  gatewayStatuses: SelectionEntityState;
  meterStatuses: SelectionEntityState;
  gateways: GatewaysState;
  measurements: MeasurementState;
  allMeters: NormalizedState<Meter>;
  users: UserState;
  organisations: NormalizedState<Organisation>;
}

export const enum RequestType {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}
