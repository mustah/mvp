import {Location} from 'history';
import {LOCATION_CHANGE} from 'react-router-redux';
import {combineReducers, Reducer} from 'redux';
import {routes} from '../../app/routes';
import {Maybe} from '../../helpers/Maybe';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../../usecases/auth/authActions';
import {SEARCH} from '../../usecases/search/searchActions';
import {ObjectsById} from '../domain-models/domainModels';
import {resetReducer} from '../domain-models/domainModelsReducer';
import {Gateway} from './gateway/gatewayModels';
import {Meter} from './meter/meterModels';
import {
  NormalizedPaginated,
  NormalizedPaginatedState,
  PageNumbered,
  PaginatedDomainModelsState,
  SingleEntityFailure,
} from './paginatedDomainModels';
import {
  domainModelPaginatedClearError,
  domainModelsPaginatedFailure,
  domainModelsPaginatedGetSuccess,
  domainModelsPaginatedRequest,
} from './paginatedDomainModelsActions';
import {
  domainModelsPaginatedEntityFailure,
  domainModelsPaginatedEntityRequest,
  domainModelsPaginatedEntitySuccess,
} from './paginatedDomainModelsEntityActions';

export const makeInitialState = <T extends Identifiable>(): NormalizedPaginatedState<T> => ({
  isFetchingSingle: false,
  nonExistingSingles: {},
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
  {payload: {page, ...error}}: Action<ErrorResponse & PageNumbered>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false, error},
  },
});

const clearError = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload: {page}}: Action<PageNumbered>,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false},
  },
});

const entityRequest = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
): NormalizedPaginatedState<T> => ({
  ...state,
  isFetchingSingle: true,
});

const addEntities = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload}: Action<T | T[]>,
): NormalizedPaginatedState<T> => {
  const newEntities: ObjectsById<T> = Array.isArray(payload)
    ? payload.reduce((prev, curr) => ({
      ...prev,
      [curr.id]: curr,
    }), {})
    : {[payload.id]: payload};
  return ({
    ...state,
    isFetchingSingle: false,
    entities: {...state.entities, ...newEntities},
  });
};

const entityFailure = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {payload}: Action<SingleEntityFailure>,
): NormalizedPaginatedState<T> => ({
  ...state,
  isFetchingSingle: false,
  nonExistingSingles: {...state.nonExistingSingles, [payload.id]: payload},
});

type ActionTypes<T extends Identifiable> =
  | Action<NormalizedPaginated<T>>
  | Action<number>
  | Action<ErrorResponse & PageNumbered>
  | Action<PageNumbered>
  | Action<T | T[]>
  | Action<SingleEntityFailure>
  | Action<Location>;

const metersReducer = <T extends Identifiable>(
  state: NormalizedPaginatedState<T> = makeInitialState<T>(),
  action: ActionTypes<T>,
): NormalizedPaginatedState<T> => {
  if (action.type === LOCATION_CHANGE) {
    const location = (action as Action<Location>).payload;
    if (location.pathname === routes.selection) {
      return {...makeInitialState()};
    }
  }
  return resetReducer<NormalizedPaginatedState<T>>(state, action, makeInitialState<T>());
};

const reducerFor = <T extends Identifiable>(
  entity: keyof PaginatedDomainModelsState,
  endPoint: EndPoints,
  additionalReducers?: Reducer<NormalizedPaginatedState<T>>,
) =>
  (
    state: NormalizedPaginatedState<T> = makeInitialState<T>(),
    action: ActionTypes<T>,
  ): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case domainModelsPaginatedRequest(endPoint):
        return setRequest(state, action as Action<number>);
      case domainModelsPaginatedGetSuccess(endPoint):
        return setEntities<T>(entity, state, action as Action<NormalizedPaginated<T>>);
      case domainModelsPaginatedFailure(endPoint):
        return setError<T>(state, action as Action<ErrorResponse & PageNumbered>);
      case domainModelPaginatedClearError(endPoint):
        return clearError(state, action as Action<PageNumbered>);
      case domainModelsPaginatedEntityRequest(endPoint):
        return entityRequest(state);
      case domainModelsPaginatedEntitySuccess(endPoint):
        return addEntities(state, action as Action<T | T[]>);
      case domainModelsPaginatedEntityFailure(endPoint):
        return entityFailure(state, action as Action<SingleEntityFailure>);
      case LOGOUT_USER:
      case SEARCH:
        return {...makeInitialState()};
      default:
        return Maybe.maybe(additionalReducers)
          .map((reducer: Reducer<NormalizedPaginatedState<T>>) => reducer(state, action))
          .orElse(resetReducer<NormalizedPaginatedState<T>>(state, action, makeInitialState<T>()));
    }
  };

export const meters = reducerFor<Meter>('meters', EndPoints.meters, metersReducer);
export const gateways = reducerFor<Gateway>('gateways', EndPoints.gateways);

export const paginatedDomainModels = combineReducers<PaginatedDomainModelsState>({
  meters,
  gateways,
});
