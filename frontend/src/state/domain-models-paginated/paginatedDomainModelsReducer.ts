import {Location} from 'history';
import {isEqual, pick} from 'lodash';
import {LOCATION_CHANGE} from 'react-router-redux';
import {combineReducers, Reducer} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {isOnSearchPage} from '../../app/routes';
import {Maybe} from '../../helpers/Maybe';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {CollectionStat} from '../domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../domain-models/domainModels';
import {search} from '../search/searchActions';
import {ApiRequestSortingOptions} from '../ui/pagination/paginationModels';
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
  sortTableAction,
} from './paginatedDomainModelsActions';
import {
  domainModelsPaginatedDeleteFailure,
  domainModelsPaginatedDeleteRequest,
  domainModelsPaginatedDeleteSuccess,
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
  page: number,
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
  payload: NormalizedPaginated<T>,
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
  {page, ...error}: ErrorResponse & PageNumbered,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false, error},
  },
});

const clearError = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {page}: PageNumbered,
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
  payload: T | T[],
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

const removePagedEntity = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {id, page}: Meter & PageNumbered,
): NormalizedPaginatedState<T> => {
  const entities = {...state.entities};
  delete entities[id];
  const uuids = state.result[page].result || [];
  return ({
    ...state,
    isFetchingSingle: false,
    entities,
    result: {
      ...state.result,
      [page]: {
        ...state.result[page],
        result: uuids.filter((it) => it !== id)
      },
    }
  });
};

const entityFailure = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  payload: SingleEntityFailure,
): NormalizedPaginatedState<T> => ({
  ...state,
  isFetchingSingle: false,
  nonExistingSingles: {...state.nonExistingSingles, [payload.id]: payload},
});

type ActionTypes<T extends Identifiable> = EmptyAction<string> |
  Action<NormalizedPaginated<T>
    | Meter & PageNumbered
    | number
    | undefined
    | ErrorResponse & PageNumbered
    | PageNumbered
    | T | T[]
    | SingleEntityFailure
    | Location
    | ApiRequestSortingOptions[]>;

const metersReducer = <T extends Identifiable>(
  state: NormalizedPaginatedState<T> = makeInitialState<T>(),
  action: ActionTypes<T>,
): NormalizedPaginatedState<T> => {
  switch (action.type) {
    case getType(sortTableAction(EndPoints.meters)):
      return sortTable(state, (action as Action<ApiRequestSortingOptions[]>).payload);
    default:
      return resetReducer<NormalizedPaginatedState<T>>(state, action, makeInitialState<T>());
  }
};

const collectionStatFacilitiesReducer = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  action: ActionTypes<T>,
): NormalizedPaginatedState<T> => {
  switch (action.type) {
    case getType(sortTableAction(EndPoints.collectionStatFacility)):
      return sortTable(state, (action as Action<ApiRequestSortingOptions[]>).payload);
    default:
      return resetReducer<NormalizedPaginatedState<T>>(state, action, makeInitialState<T>());
  }
};

const sortTable = <T extends Identifiable = Identifiable>(
  state: NormalizedPaginatedState<T>,
  sortingOptions: ApiRequestSortingOptions[] | undefined
): NormalizedPaginatedState<T> => {
  if (isEqual(state.sort!, sortingOptions)) {
    return state;
  }

  const newState: NormalizedPaginatedState<T> = {...state, result: {}};
  if (sortingOptions) {
    newState.sort = [...sortingOptions];
  } else {
    delete newState.sort;
  }
  return newState;
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
        return setRequest(state, (action as Action<number>).payload);
      case domainModelsPaginatedGetSuccess(endPoint):
        return setEntities<T>(entity, state, (action as Action<NormalizedPaginated<T>>).payload);
      case domainModelsPaginatedFailure(endPoint):
        return setError<T>(state, (action as Action<ErrorResponse & PageNumbered>).payload);
      case domainModelPaginatedClearError(endPoint):
        return clearError(state, (action as Action<PageNumbered>).payload);
      case domainModelsPaginatedEntityRequest(endPoint):
      case domainModelsPaginatedDeleteRequest(endPoint):
        return entityRequest(state);
      case domainModelsPaginatedEntitySuccess(endPoint):
        return addEntities(state, (action as Action<T | T[]>).payload);
      case domainModelsPaginatedDeleteSuccess(endPoint):
        return removePagedEntity(state, (action as Action<Meter & PageNumbered>).payload);
      case domainModelsPaginatedEntityFailure(endPoint):
      case domainModelsPaginatedDeleteFailure(endPoint):
        return entityFailure(state, (action as Action<SingleEntityFailure>).payload);
      case LOCATION_CHANGE:
        return isOnSearchPage((action as Action<Location>).payload)
          ? state
          : {
            ...makeInitialState<T>(),
            ...pick(state, ['sort']),
          };
      case getType(search):
        return {...makeInitialState<T>()};
      default:
        return Maybe.maybe(additionalReducers)
          .map((reducer: Reducer<NormalizedPaginatedState<T>>) => reducer(state, action))
          .orElse(resetReducer<NormalizedPaginatedState<T>>(state, action, makeInitialState<T>()));
    }
  };

export const meters = reducerFor<Meter>('meters', EndPoints.meters, metersReducer);
export const gateways = reducerFor<Gateway>('gateways', EndPoints.gateways);
export const collectionStatFacilities = reducerFor<CollectionStat>(
  'collectionStatFacilities',
  EndPoints.collectionStatFacility,
  collectionStatFacilitiesReducer
);

export const paginatedDomainModels = combineReducers<PaginatedDomainModelsState>({
  meters,
  gateways,
  collectionStatFacilities,
});
