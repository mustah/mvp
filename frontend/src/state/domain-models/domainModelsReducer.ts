import {EmptyAction} from 'react-redux-typescript';
import {combineReducers} from 'redux';
import {Action, ErrorResponse, HasId, uuid} from '../../types/Types';
import {DomainModelsState, EndPoints, Normalized, NormalizedState, ObjectsById, SelectionEntity} from './domainModels';
import {
  DOMAIN_MODELS_DELETE_SUCCESS,
  DOMAIN_MODELS_FAILURE,
  DOMAIN_MODELS_GET_ENTITY_SUCCESS,
  DOMAIN_MODELS_GET_SUCCESS,
  DOMAIN_MODELS_POST_SUCCESS,
  DOMAIN_MODELS_PUT_SUCCESS,
  DOMAIN_MODELS_REQUEST,
} from './domainModelsActions';
import {Gateway} from './gateway/gatewayModels';
import {Measurement} from './measurement/measurementModels';
import {User} from './user/userModels';

export const initialDomain = <T extends HasId>(): NormalizedState<T> => ({
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
});

const setEntities = <T extends HasId>(
  entity: string,
  state: NormalizedState<T>,
  {payload}: Action<Normalized<T>>,
): NormalizedState<T> => {
  const result: uuid[] = Array.isArray(payload.result) ? payload.result : payload.result[entity];
  const entities: ObjectsById<T> = payload.entities[entity];
  return {
    ...state,
    isFetching: false,
    entities,
    result,
    total: result.length,
  };
};

const addEntity =
  <T extends HasId>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    const payload = action.payload;
    const result: uuid[] = [...state.result, payload.id];
    const entities: ObjectsById<T> = {...state.entities};
    entities[payload.id] = payload;
    return {
      ...state,
      isFetching: false,
      entities,
      result,
      total: result.length,
    };
  };

const modifyEntity =
  <T extends HasId>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    const payload = action.payload;
    const entities: ObjectsById<T> = {...state.entities};
    entities[payload.id] = payload;
    return {
      ...state,
      isFetching: false,
      entities,
    };
  };

const removeEntity =
  <T extends HasId>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    // TODO do we need to introduce a domain model interface with id: uuid in order to avoid "as any" below?
    const payload = action.payload;
    const result: uuid[] = state.result.filter((id) => id !== payload.id);
    const entities: ObjectsById<T> = {...state.entities};
    delete entities[payload.id];
    return {
      ...state,
      entities,
      isFetching: false,
      result,
      total: result.length,
    };
  };

type ActionTypes<T extends HasId> =
  | EmptyAction<string>
  | Action<Normalized<T>>
  | Action<T>
  | Action<ErrorResponse>;

// TODO: Add tests for PUT, POST, DELETE
const reducerFor = <T extends HasId>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedState<T> = initialDomain<T>(), action: ActionTypes<T>): NormalizedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_REQUEST(endPoint):
        return {
          ...state,
          isFetching: true,
        };
      case DOMAIN_MODELS_GET_SUCCESS(endPoint):
        return setEntities<T>(entity, state, action as Action<Normalized<T>>);
      // TODO: Add tests
      case DOMAIN_MODELS_GET_ENTITY_SUCCESS(endPoint):
        return addEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_POST_SUCCESS(endPoint):
        return addEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_PUT_SUCCESS(endPoint):
        return modifyEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_DELETE_SUCCESS(endPoint):
        return removeEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_FAILURE(endPoint):
        return {
          ...state,
          isFetching: false,
          error: {...(action as Action<ErrorResponse>).payload},
        };
      default:
        return state;
    }
  };

export const addresses = reducerFor<SelectionEntity>('addresses', EndPoints.selections);
export const alarms = reducerFor<SelectionEntity>('alarms', EndPoints.selections);
export const cities = reducerFor<SelectionEntity>('cities', EndPoints.selections);
export const gatewayStatuses = reducerFor<SelectionEntity>('gatewayStatuses', EndPoints.selections);
export const gateways = reducerFor<Gateway>('gateways', EndPoints.gateways);
export const manufacturers = reducerFor<SelectionEntity>('manufacturers', EndPoints.selections);
export const meterStatuses = reducerFor<SelectionEntity>('meterStatuses', EndPoints.selections);
export const productModels = reducerFor<SelectionEntity>('productModels', EndPoints.selections);
export const measurements = reducerFor<Measurement>('measurements', EndPoints.measurements);
export const users = reducerFor<User>('users', EndPoints.users);

export const domainModels = combineReducers<DomainModelsState>({
  addresses,
  alarms,
  cities,
  gatewayStatuses,
  gateways,
  manufacturers,
  meterStatuses,
  productModels,
  users,
  measurements,
});
