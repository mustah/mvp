import {EmptyAction} from 'react-redux-typescript';
import {Action, ErrorResponse} from '../../types/Types';
import {ObjectsById, EndPoints} from './domainModels';
import {Measurement} from './measurement/measurementModels';
import {Meter} from './meter/meterModels';
import {NormalizedPaginated, NormalizedPaginatedState, PaginationMetadata} from './paginatedDomainModels';
import {
  DOMAIN_MODELS_PAGINATED_FAILURE,
  DOMAIN_MODELS_PAGINATED_GET_SUCCESS,
  DOMAIN_MODELS_PAGINATED_REQUEST,
} from './paginatedDomainModelsActions';

export const initialPaginatedDomain = <T>(): NormalizedPaginatedState<T> => ({
  result: {},
  entities: {},
});

const setEntities =
  <T>(entity: string,
      state: NormalizedPaginatedState<T>,
      {payload}: Action<NormalizedPaginated<T>>): NormalizedPaginatedState<T> => {
    const result: PaginationMetadata = payload.result;
    const entities: ObjectsById<T> = payload.entities[entity];
    return {
      ...state,
      entities,
      result,
    };
  };

type ActionTypes<T> =
  | EmptyAction<string>
  | Action<NormalizedPaginated<T>>
  | Action<ErrorResponse>;

const reducerFor = <T>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
   action: ActionTypes<T>): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_PAGINATED_REQUEST.concat(endPoint):
        return {
          ...state,
        };
      case DOMAIN_MODELS_PAGINATED_GET_SUCCESS.concat(endPoint):
        return setEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case DOMAIN_MODELS_PAGINATED_FAILURE.concat(endPoint):
        return {
          ...state,
          error: {...(action as Action<ErrorResponse>).payload},
        };
      default:
        return state;
    }
  };

export const paginatedMeters = reducerFor<Meter>('meters', EndPoints.meters);
export const paginatedMeasurements = reducerFor<Measurement>('meters', EndPoints.measurements);
