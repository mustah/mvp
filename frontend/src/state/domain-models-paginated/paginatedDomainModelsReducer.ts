import {combineReducers} from 'redux';
import {Action, ErrorResponse, HasId, uuid} from '../../types/Types';
import {EndPoints, ObjectsById} from '../domain-models/domainModels';
import {
  ADD_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  UPDATE_SELECTION,
} from '../search/selection/selectionActions';
import {Meter} from './meter/meterModels';
import {
  HasPageNumber,
  NormalizedPaginated,
  NormalizedPaginatedState,
  PaginatedDomainModelsState,
} from './paginatedDomainModels';
import {
  DOMAIN_MODELS_PAGINATED_CLEAR_ERROR,
  DOMAIN_MODELS_PAGINATED_FAILURE,
  DOMAIN_MODELS_PAGINATED_GET_SUCCESS,
  DOMAIN_MODELS_PAGINATED_REQUEST,
} from './paginatedDomainModelsActions';

export const initialPaginatedDomain = <T extends HasId>(): NormalizedPaginatedState<T> => ({
  result: {},
  entities: {},
});
const setRequest = <T extends HasId>(
  state: NormalizedPaginatedState<T>,
  {payload: page}: Action<number>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isFetching: true, isSuccessfullyFetched: false},
  },
});

const setEntities = <T extends HasId>(
  entity: string,
  state: NormalizedPaginatedState<T>,
  {payload}: Action<NormalizedPaginated<T>>,
): NormalizedPaginatedState<T> => {
  const page: number = payload.page;
  const content: uuid[] = payload.result.content;
  const entities: ObjectsById<T> = payload.entities[entity];
  return {
    ...state,
    entities: {...state.entities, ...entities},
    result: {
      ...state.result,
      [page]: {result: content, isFetching: false, isSuccessfullyFetched: true},
    },
  };
};

const setError = <T extends HasId>(
  state: NormalizedPaginatedState<T>,
  {payload: {page, ...error}}: Action<ErrorResponse & HasPageNumber>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false, error},
  },
});

const clearError = <T extends HasId>(
  state: NormalizedPaginatedState<T>,
  {payload: {page}}: Action<HasPageNumber>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false},
  },
});

type ActionTypes<T extends HasId> =
  | Action<NormalizedPaginated<T>>
  | Action<number>
  | Action<ErrorResponse & HasPageNumber>
  | Action<HasPageNumber>;

export const reducerFor = <T extends HasId>(entity: keyof PaginatedDomainModelsState, endPoint: EndPoints) =>
  (
    state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
    action: ActionTypes<T>,
  ): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_PAGINATED_REQUEST(endPoint):
        return setRequest(state, action as Action<number>);
      case DOMAIN_MODELS_PAGINATED_GET_SUCCESS(endPoint):
        return setEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case DOMAIN_MODELS_PAGINATED_FAILURE(endPoint):
        return setError<T>(state, action as Action<ErrorResponse & HasPageNumber>);
      case DOMAIN_MODELS_PAGINATED_CLEAR_ERROR(endPoint):
        return clearError(state, action as Action<HasPageNumber>);
      case SELECT_SAVED_SELECTION:
      case ADD_SELECTION:
      case DESELECT_SELECTION:
      case UPDATE_SELECTION:
      case RESET_SELECTION:
      case SET_SELECTION:
      case SELECT_PERIOD:
        return {...initialPaginatedDomain<T>()};
      default:
        return state;
    }
  };

export const meters = reducerFor<Meter>('meters', EndPoints.meters);

export const paginatedDomainModels = combineReducers<PaginatedDomainModelsState>({
  meters,
});