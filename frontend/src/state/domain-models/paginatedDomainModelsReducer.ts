import {Action, ErrorResponse, HasId, uuid} from '../../types/Types';
import {EndPoints, ObjectsById} from './domainModels';
import {Measurement} from './measurement/measurementModels';
import {Meter} from './meter/meterModels';
import {
  HasComponentId,
  NormalizedPaginated,
  NormalizedPaginatedState,
  PaginationMetadata,
} from './paginatedDomainModels';
import {
  DOMAIN_MODELS_PAGINATED_FAILURE,
  DOMAIN_MODELS_PAGINATED_GET_SUCCESS,
  DOMAIN_MODELS_PAGINATED_REQUEST,
} from './paginatedDomainModelsActions';

export const initialPaginatedDomain = <T extends HasId>(): NormalizedPaginatedState<T> => ({
  result: {},
  entities: {},
});
const setRequest = <T extends HasId>(
  entity: string,
  state: NormalizedPaginatedState<T>,
  {payload}: Action<uuid>,
): NormalizedPaginatedState<T> => {
  const componentId: uuid = payload;
  return {
    ...state,
    result: {
      ...state.result,
      [componentId]: {...state.result[componentId], isFetching: true},
    },
  };
};

const setEntities = <T extends HasId>(
  entity: string,
  state: NormalizedPaginatedState<T>,
  {payload}: Action<NormalizedPaginated<T>>,
): NormalizedPaginatedState<T> => {
  const componentId: uuid = payload.componentId;
  const result: PaginationMetadata = payload.result;
  const entities: ObjectsById<T> = payload.entities[entity];
  return {
    ...state,
    entities: {...state.entities, ...entities},
    result: {
      ...state.result,
      [componentId]: {...result, isFetching: false},
    },
  };
};

const setFailure = <T extends HasId>(
  entity: string,
  state: NormalizedPaginatedState<T>,
  {payload: {componentId, ...error}}: Action<ErrorResponse & HasComponentId>,
): NormalizedPaginatedState<T> => {

  return {
    ...state,
    result: {
      ...state.result,
      [componentId]: {...state.result[componentId], error, isFetching: false},
    },
  };
};

type ActionTypes<T extends HasId> =
  | Action<NormalizedPaginated<T>>
  | Action<uuid>
  | Action<ErrorResponse & HasComponentId>;

export const reducerFor = <T extends HasId>(entity: string, endPoint: EndPoints) =>
  (
    state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
    action: ActionTypes<T>,
  ): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_PAGINATED_REQUEST(endPoint):
        return setRequest(entity, state, action as Action<uuid>);
      case DOMAIN_MODELS_PAGINATED_GET_SUCCESS(endPoint):
        return setEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case DOMAIN_MODELS_PAGINATED_FAILURE(endPoint):
        return setFailure<T>(entity, state, action as Action<ErrorResponse & HasComponentId>);
      default:
        return state;
    }
  };

export const paginatedMeters = reducerFor<Meter>('meters', EndPoints.meters);
export const paginatedMeasurements = reducerFor<Measurement>('measurements', EndPoints.measurements);
