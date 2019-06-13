import {Location} from 'history';
import {isEqual} from 'lodash';
import {combineReducers, Reducer} from 'redux';
import {ActionType, getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Identifiable, Sectors, uuid} from '../../types/Types';
import {CollectionStat} from '../domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../domain-models/domainModels';
import {search} from '../search/searchActions';
import {SortOption} from '../ui/pagination/paginationModels';
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
  sortCollectionStats,
  sortMeterCollectionStats,
  sortMeters,
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
    [page]: {isFetching: true, isSuccessfullyFetched: false, result: []},
  },
});

const setEntities = <T extends Identifiable>(
  entity: string,
  state: NormalizedPaginatedState<T>,
  payload: NormalizedPaginated<T>,
): NormalizedPaginatedState<T> => {
  const page: number = payload.page;
  const result: uuid[] = payload.result.content;
  const entities: ObjectsById<T> = payload.entities[entity];
  return {
    ...state,
    entities: {...state.entities, ...entities},
    result: {
      ...state.result,
      [page]: {isFetching: false, isSuccessfullyFetched: true, result},
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
    [page]: {isSuccessfullyFetched: false, isFetching: false, error, result: []},
  },
});

const clearError = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  {page}: PageNumbered,
): NormalizedPaginatedState<T> => ({
  ...state,
  result: {
    ...state.result,
    [page]: {isSuccessfullyFetched: false, isFetching: false, result: []},
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
  const result = state.result[page].result.filter(it => it !== id);
  return ({
    ...state,
    isFetchingSingle: false,
    entities,
    result: {
      ...state.result,
      [page]: {...state.result[page], result}
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

type SortActionTypes = ActionType<typeof sortMeters
  | typeof sortCollectionStats
  | typeof sortMeterCollectionStats>;

type ActionTypes<T extends Identifiable> =
  SortActionTypes |
  EmptyAction<string> |
  Action<NormalizedPaginated<T>
    | Meter & PageNumbered
    | number
    | undefined
    | ErrorResponse & PageNumbered
    | PageNumbered
    | T | T[]
    | SingleEntityFailure
    | Location
    | SortOption[]>;

const sortTable = <T extends Identifiable = Identifiable>(
  state: NormalizedPaginatedState<T>,
  sortingOptions: SortOption[] | undefined
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

const actionCreators = [sortCollectionStats, sortMeterCollectionStats, sortMeters];

const makeSortableReducer = <T extends Identifiable>() => (
  state: NormalizedPaginatedState<T>,
  action: SortActionTypes,
): NormalizedPaginatedState<T> =>
  actionCreators.some(actionCreator => getType(actionCreator) === action.type)
    ? sortTable(state, action.payload)
    : resetReducer(state, action, makeInitialState());

const reducerFor = <T extends Identifiable>(
  entity: keyof PaginatedDomainModelsState,
  actionKey: EndPoints | Sectors,
  additionalReducers?: Reducer<NormalizedPaginatedState<T>>,
) =>
  (
    state: NormalizedPaginatedState<T> = makeInitialState(),
    action: ActionTypes<T>
  ): NormalizedPaginatedState<T> => {
    switch (action.type) {
      case domainModelsPaginatedRequest(actionKey):
        return setRequest(state, (action as Action<number>).payload);
      case domainModelsPaginatedGetSuccess(actionKey):
        return setEntities<T>(entity, state, (action as Action<NormalizedPaginated<T>>).payload);
      case domainModelsPaginatedFailure(actionKey):
        return setError<T>(state, (action as Action<ErrorResponse & PageNumbered>).payload);
      case domainModelPaginatedClearError(actionKey):
        return clearError(state, (action as Action<PageNumbered>).payload);
      case domainModelsPaginatedEntityRequest(actionKey):
      case domainModelsPaginatedDeleteRequest(actionKey):
        return entityRequest(state);
      case domainModelsPaginatedEntitySuccess(actionKey):
        return addEntities(state, (action as Action<T | T[]>).payload);
      case domainModelsPaginatedDeleteSuccess(actionKey):
        return removePagedEntity(state, (action as Action<Meter & PageNumbered>).payload);
      case domainModelsPaginatedEntityFailure(actionKey):
      case domainModelsPaginatedDeleteFailure(actionKey):
        return entityFailure(state, (action as Action<SingleEntityFailure>).payload);
      case getType(search):
        return makeInitialState();
      default:
        return additionalReducers
          ? additionalReducers(state, action)
          : resetReducer(state, action, makeInitialState());
    }
  };

export const gateways = reducerFor<Gateway>('gateways', EndPoints.gateways);

export const meters = reducerFor<Meter>(
  'meters',
  EndPoints.meters,
  makeSortableReducer(),
);

export const collectionStatFacilities = reducerFor<CollectionStat>(
  'collectionStatFacilities',
  Sectors.collectionStatFacilities,
  makeSortableReducer(),
);

export const meterCollectionStatFacilities = reducerFor<CollectionStat>(
  'collectionStatFacilities',
  Sectors.meterCollectionStatFacilities,
  makeSortableReducer(),
);

export const paginatedDomainModels = combineReducers<PaginatedDomainModelsState>({
  meters,
  gateways,
  collectionStatFacilities,
  meterCollectionStatFacilities,
});
