import {EmptyAction} from 'react-redux-typescript';
import {Action, ErrorResponse, HasId, uuid} from '../../types/Types';
import {EndPoints, ObjectsById} from './domainModels';
import {Measurement} from './measurement/measurementModels';
import {Meter} from './meter/meterModels';
import {
  NormalizedPaginated,
  NormalizedPaginatedState,
  PaginationMetadata
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

const setEntities =
  <T extends HasId>(entity: string,
                    state: NormalizedPaginatedState<T>,
                    {payload}: Action<NormalizedPaginated<T> & {componentId: uuid}>): NormalizedPaginatedState<T> => {
    const objId: uuid = payload.componentId;
    const result: PaginationMetadata = payload.result;
    const entities: ObjectsById<T> = payload.entities[entity];
    return {
      ...state,
      entities,
      result: {
        [objId]: {...result, isFetching: false},
      },
    };
  };

type ActionTypes<T extends HasId> =
  | EmptyAction<string>
  | Action<NormalizedPaginated<T>>
  | Action<ErrorResponse>;

export const reducerFor = <T extends HasId>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
   action: ActionTypes<T>): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_PAGINATED_REQUEST.concat(endPoint):
        return {
          ...state,
        };
      case DOMAIN_MODELS_PAGINATED_GET_SUCCESS(endPoint):
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
export const paginatedMeasurements = reducerFor<Measurement>('measurements', EndPoints.measurements);
