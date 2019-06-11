import {ActionType, getType} from 'typesafe-actions';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {UseCases} from '../../../types/Types';
import {sortMeters} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {search} from '../../search/searchActions';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from './paginationActions';
import {
  ChangePagePayload,
  EntityTyped,
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

const updateMetaData = (
  state: PaginationState,
  {entityType, totalElements, totalPages}: PaginationMetadataPayload,
): PaginationState => ({
  ...state,
  [entityType]: {...state[entityType], size: paginationPageSize, totalElements, totalPages},
});

type ActionTypes = ActionType<typeof changePage
  | typeof updatePageMetaData
  | typeof search
  | typeof resetSelection
  | typeof sortMeters>;

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
        .map(_ => resetStateForEntityType(state, {entityType: 'meters'}))
        .orElse(state);
    case getType(sortMeters):
      return resetStateForEntityType(state, {entityType: 'meters'});
    default:
      return resetReducer<PaginationState>(state, action, initialState);
  }
};
