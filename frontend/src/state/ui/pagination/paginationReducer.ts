import {ActionType, getType} from 'typesafe-actions';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {UseCases} from '../../../types/Types';
import {search} from '../../search/searchActions';
import {Query, QueryParameter} from '../../search/searchModels';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from './paginationActions';
import {
  PaginationChangePayload,
  PaginationMetadataPayload,
  PaginationModel,
  PaginationState,
} from './paginationModels';

export const paginationPageSize = 20;

export const initialPaginationModel: PaginationModel = {
  useCases: {},
  totalElements: -1,
  totalPages: -1,
  size: paginationPageSize,
};

export const initialState: PaginationState = {
  meters: {...initialPaginationModel},
  gateways: {...initialPaginationModel},
  collectionStatFacilities: {...initialPaginationModel},
  meterCollectionStatFacilities: {...initialPaginationModel},
};

const onChangePage = (
  state: PaginationState,
  {entityType, componentId, page}: PaginationChangePayload,
): PaginationState => ({
  ...state,
  [entityType]: {
    ...state[entityType],
    useCases: {...state[entityType]!.useCases, [componentId]: {page}},
  },
});

const updateMetaData = (
  state: PaginationState,
  {entityType, totalElements, totalPages}: PaginationMetadataPayload,
): PaginationState => ({
  ...state,
  [entityType]: {useCases: {}, size: paginationPageSize, ...state[entityType], totalElements, totalPages},
});

const hasQuery = ({query}: Query) => isDefined(query);

const resetSearchResultPage: PaginationChangePayload = {
  entityType: 'meters',
  componentId: 'searchResultList',
  page: 0,
};

const resetMetersPage: PaginationChangePayload = {
  entityType: 'meters',
  componentId: 'validationMeterList',
  page: 0,
};

const toPaginationChangePayload = (payload: QueryParameter): PaginationChangePayload =>
  Maybe.maybe(payload[UseCases.collection])
    .filter(hasQuery)
    .map(() => resetSearchResultPage)
    .orElse(resetMetersPage);

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
      return onChangePage(state, toPaginationChangePayload(action.payload));
    default:
      return resetReducer<PaginationState>(state, action, initialState);
  }
};
