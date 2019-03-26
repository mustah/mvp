import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {Action, UseCases} from '../../../types/Types';
import {search} from '../../search/searchActions';
import {Query, QueryParameter} from '../../search/searchModels';
import {resetReducer} from '../../../reducers/resetReducer';
import {CHANGE_PAGE, UPDATE_PAGE_METADATA} from './paginationActions';
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

export const initialPaginationState: PaginationState = {
  meters: {...initialPaginationModel},
  gateways: {...initialPaginationModel},
  collectionStatFacilities: {...initialPaginationModel},
};

type ActionTypes =
  | Action<PaginationMetadataPayload>
  | Action<PaginationChangePayload>
  | Action<QueryParameter>
  | EmptyAction<string>;

const changePage = (
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
  {payload: {entityType, totalElements, totalPages}}: Action<PaginationMetadataPayload>,
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

export const pagination = (
  state: PaginationState = initialPaginationState,
  action: ActionTypes,
) => {
  switch (action.type) {
    case CHANGE_PAGE:
      return changePage(state, (action as Action<PaginationChangePayload>).payload);
    case UPDATE_PAGE_METADATA:
      return updateMetaData(state, action as Action<PaginationMetadataPayload>);
    case getType(search):
      return changePage(state, toPaginationChangePayload((action as Action<QueryParameter>).payload));
    default:
      return resetReducer<PaginationState>(state, action, {...initialPaginationState});
  }
};
