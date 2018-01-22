import {EmptyAction} from 'react-redux-typescript';
import {combineReducers} from 'redux';
import {Action, ErrorResponse, uuid} from '../../types/Types';
import {
  DomainModel,
  DomainModelsState,
  EndPoints,
  Normalized,
  NormalizedPaginated,
  NormalizedPaginatedState,
  NormalizedState,
  PaginatedResult,
  SelectionEntity,
} from './domainModels';
import {
  DOMAIN_MODELS_DELETE_SUCCESS,
  DOMAIN_MODELS_FAILURE,
  DOMAIN_MODELS_GET_ENTITY_SUCCESS,
  DOMAIN_MODELS_GET_SUCCESS,
  DOMAIN_MODELS_PAGINATED_FAILURE,
  DOMAIN_MODELS_PAGINATED_GET_SUCCESS,
  DOMAIN_MODELS_PAGINATED_REQUEST,
  DOMAIN_MODELS_POST_SUCCESS,
  DOMAIN_MODELS_PUT_SUCCESS,
  DOMAIN_MODELS_REQUEST,
} from './domainModelsActions';
import {Gateway} from './gateway/gatewayModels';
import {Measurement} from './measurement/measurementModels';
import {Meter} from './meter/meterModels';
import {User} from './user/userModels';

export const initialDomain = <T>(): NormalizedState<T> => ({
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
});

export const initialPaginatedDomain = <T>(): NormalizedPaginatedState<T> => ({
  result: {
    content: [],
    first: false,
    last: false,
    number: 0,
    numberOfElements: 0,
    size: 0,
    sort: null,
    totalElements: 0,
    totalPages: 0,
  },
  entities: {},
  isFetching: false,
});

const setEntities =
  <T>(entity: string, state: NormalizedState<T>, {payload}: Action<Normalized<T>>): NormalizedState<T> => {
    const result: uuid[] = Array.isArray(payload.result) ? payload.result : payload.result[entity];
    const entities: any = payload.entities[entity];
    return {
      ...state,
      isFetching: false,
      entities,
      result,
      total: result.length,
    };
  };

const setPaginatedEntities =
  <T>(entity: string,
      state: NormalizedPaginated<T>,
      {payload}: Action<NormalizedPaginated<T>>): NormalizedPaginatedState<T> => {
    const result: PaginatedResult = payload.result;
    const entities: any = payload.entities[entity];
    return {
      ...state,
      isFetching: false,
      entities,
      result,
    };
  };

const addEntity =
  <T>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    const payload = action.payload as any;
    const result: uuid[] = [...state.result, payload.id];
    const entities: DomainModel<T> = {...state.entities};
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
  <T>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    const payload = action.payload as any;
    const entities: DomainModel<T> = {...state.entities};
    entities[payload.id] = payload;
    return {
      ...state,
      isFetching: false,
      entities,
    };
  };

const removeEntity =
  <T>(entity: string, state: NormalizedState<T>, action: Action<T>): NormalizedState<T> => {
    // TODO do we need to introduce a domain model interface with id: uuid in order to avoid "as any" below?
    const payload = action.payload as any;
    const result: uuid[] = state.result.filter((id) => id !== payload.id);
    const entities: DomainModel<T> = {...state.entities};
    delete entities[payload.id];
    return {
      ...state,
      entities,
      isFetching: false,
      result,
      total: result.length,
    };
  };

type ActionTypes<T> =
  | EmptyAction<string>
  | Action<Normalized<T>>
  | Action<ErrorResponse>;

type PaginatedActionTypes<T> =
  | EmptyAction<string>
  | Action<NormalizedPaginated<T>>
  | Action<ErrorResponse>;

// TODO: Add tests for PUT, POST, DELETE
const reducerFor = <T>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedState<T> = initialDomain<T>(), action: ActionTypes<T>): NormalizedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_REQUEST.concat(endPoint):
        return {
          ...state,
          isFetching: true,
        };
      case DOMAIN_MODELS_GET_SUCCESS.concat(endPoint):
        return setEntities<T>(entity, state, action as Action<Normalized<T>>);
      // TODO: Add tests
      case DOMAIN_MODELS_GET_ENTITY_SUCCESS.concat(endPoint):
        return addEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_POST_SUCCESS.concat(endPoint):
        return addEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_PUT_SUCCESS.concat(endPoint):
        return modifyEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_DELETE_SUCCESS.concat(endPoint):
        return removeEntity<T>(entity, state, action as Action<T>);
      case DOMAIN_MODELS_FAILURE.concat(endPoint):
        return {
          ...state,
          isFetching: false,
          error: {...(action as Action<ErrorResponse>).payload},
        };
      default:
        return state;
    }
  };

const reducerForPaginated = <T>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
   action: PaginatedActionTypes<T>): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_PAGINATED_REQUEST.concat(endPoint):
        return {
          ...state,
          isFetching: true,
        };
      case DOMAIN_MODELS_PAGINATED_GET_SUCCESS.concat(endPoint):
        return setPaginatedEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case DOMAIN_MODELS_PAGINATED_FAILURE.concat(endPoint):
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
export const measurements = reducerForPaginated<Measurement>('measurements', EndPoints.measurements);
export const meterStatuses = reducerFor<SelectionEntity>('meterStatuses', EndPoints.selections);
export const meters = reducerFor<Meter>('meters', EndPoints.meters);
export const productModels = reducerFor<SelectionEntity>('productModels', EndPoints.selections);
export const users = reducerFor<User>('users', EndPoints.users);

export const domainModels = combineReducers<DomainModelsState>({
  addresses,
  alarms,
  cities,
  gatewayStatuses,
  gateways,
  manufacturers,
  measurements,
  meterStatuses,
  meters,
  productModels,
  users,
});
