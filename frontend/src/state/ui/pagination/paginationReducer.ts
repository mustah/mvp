import {ActionType, getType} from 'typesafe-actions';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {UseCases} from '../../../types/Types';
import {
  sortBatchReferences,
  sortCollectionStats,
  sortMeterCollectionStats,
  sortMeters
} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {search} from '../../search/searchActions';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from './paginationActions';
import {
  ChangePagePayload,
  EntityTyped,
  EntityTypes,
  Pagination,
  PaginationMetadataPayload,
  PaginationState
} from './paginationModels';

export const paginationPageSize = 50;

export const initialPagination: Pagination = {
  page: 0,
  size: paginationPageSize,
  totalElements: -1,
  totalPages: -1,
};

export const initialState: PaginationState = {
  batchReferences: {...initialPagination},
  meters: {...initialPagination},
  gateways: {...initialPagination},
  collectionStatFacilities: {...initialPagination},
  meterCollectionStatFacilities: {...initialPagination},
};

const updatePage = (state: PaginationState, {entityType, page}: ChangePagePayload): PaginationState => ({
  ...state,
  [entityType]: {...state[entityType], page},
});

const resetStateForEntityType = (state: PaginationState, {entityType}: EntityTyped): PaginationState => ({
  ...state,
  [entityType]: initialPagination
});

const resetStateForAll = (state: PaginationState, entityTypes: EntityTypes[]) =>
  entityTypes.reduce((prev, curr) => resetStateForEntityType(prev, {entityType: curr}), state);

const updateMetaData = (
  state: PaginationState,
  {entityType, totalElements, totalPages}: PaginationMetadataPayload,
): PaginationState => ({
  ...state,
  [entityType]: {...state[entityType], size: paginationPageSize, totalElements, totalPages},
});

const entityTypes: EntityTypes[] = [
  'batchReferences',
  'meters',
  'collectionStatFacilities',
  'meterCollectionStatFacilities'
];

type ActionTypes = ActionType<typeof changePage
  | typeof updatePageMetaData
  | typeof search
  | typeof resetSelection
  | typeof sortBatchReferences
  | typeof sortMeters
  | typeof sortCollectionStats
  | typeof sortMeterCollectionStats>;

export const pagination = (state: PaginationState = initialState, action: ActionTypes): PaginationState => {
  switch (action.type) {
    case getType(changePage):
      return updatePage(state, action.payload);
    case getType(updatePageMetaData):
      return updateMetaData(state, action.payload);
    case getType(search):
      return Maybe.maybe(action.payload[UseCases.validation])
        .map(it => it.query)
        .filter(isDefined)
        .map(_ => resetStateForAll(state, entityTypes))
        .orElse(state);
    case getType(sortBatchReferences):
      return resetStateForEntityType(state, {entityType: 'batchReferences'});
    case getType(sortMeters):
      return resetStateForEntityType(state, {entityType: 'meters'});
    case getType(sortCollectionStats):
      return resetStateForEntityType(state, {entityType: 'collectionStatFacilities'});
    case getType(sortMeterCollectionStats):
      return resetStateForEntityType(state, {entityType: 'meterCollectionStatFacilities'});
    default:
      return resetReducer<PaginationState>(state, action, initialState);
  }
};
