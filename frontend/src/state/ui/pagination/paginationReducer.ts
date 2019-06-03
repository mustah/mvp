import {ActionType, getType} from 'typesafe-actions';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {UseCases} from '../../../types/Types';
import {search} from '../../search/searchActions';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from './paginationActions';
import {ChangePagePayload, Pagination, PaginationMetadataPayload, PaginationState} from './paginationModels';

export const paginationPageSize = 50;

const initialPagination: Pagination = {
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

const onChangePage = (
  state: PaginationState,
  {entityType, page}: ChangePagePayload,
): PaginationState => ({
  ...state,
  [entityType]: {...state[entityType], page},
});

const updateMetaData = (
  state: PaginationState,
  {entityType, totalElements, totalPages}: PaginationMetadataPayload,
): PaginationState => ({
  ...state,
  [entityType]: {...state[entityType], size: paginationPageSize, totalElements, totalPages},
});

const resetSearchResultPage: ChangePagePayload = {
  entityType: 'meters',
  page: 0,
};

type ActionTypes = ActionType<typeof changePage | typeof updatePageMetaData | typeof search | typeof resetSelection>;

export const pagination = (
  state: PaginationState = initialState,
  action: ActionTypes,
): PaginationState => {
  switch (action.type) {
    case getType(changePage):
      return onChangePage(state, action.payload);
    case getType(updatePageMetaData):
      return updateMetaData(state, action.payload);
    case getType(search):
      const payload = action.payload[UseCases.validation] || action.payload[UseCases.collection];
      return Maybe.maybe(payload)
        .filter(isDefined)
        .map(_ => onChangePage(state, resetSearchResultPage))
        .orElse(state);
    default:
      return resetReducer<PaginationState>(state, action, initialState);
  }
};
