import {combineReducers} from 'redux';
import {Action, ErrorResponse, Identifiable, uuid} from '../../types/Types';
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
  domainModelPaginatedClearError,
  domainModelsPaginatedFailure,
  domainModelsPaginatedGetSuccess,
  domainModelsPaginatedRequest,
} from './paginatedDomainModelsActions';

export const initialPaginatedDomain = <T extends Identifiable>(): NormalizedPaginatedState<T> => ({
  result: {},
  entities: {},
});
const setRequest = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload: page}: Action<number>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isFetching: true, isSuccessfullyFetched: false},
  },
});

const setEntities = <T extends Identifiable>(
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

const setError = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload: {page, ...error}}: Action<ErrorResponse & HasPageNumber>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false, error},
  },
});

const clearError = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload: {page}}: Action<HasPageNumber>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false},
  },
});

type ActionTypes<T extends Identifiable> =
  | Action<NormalizedPaginated<T>>
  | Action<number>
  | Action<ErrorResponse & HasPageNumber>
  | Action<HasPageNumber>;

export const reducerFor = <T extends Identifiable>(
  entity: keyof PaginatedDomainModelsState,
  endPoint: EndPoints,
) =>
  (
    state: NormalizedPaginatedState<T> = initialPaginatedDomain<T>(),
    action: ActionTypes<T>,
  ): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case domainModelsPaginatedRequest(endPoint):
        return setRequest(state, action as Action<number>);
      case domainModelsPaginatedGetSuccess(endPoint):
        return setEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case domainModelsPaginatedFailure(endPoint):
        return setError<T>(state, action as Action<ErrorResponse & HasPageNumber>);
      case domainModelPaginatedClearError(endPoint):
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
