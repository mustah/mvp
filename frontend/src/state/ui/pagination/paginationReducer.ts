import {EmptyAction} from 'react-redux-typescript';
import {isDefined} from '../../../helpers/commonUtils';
import {Maybe} from '../../../helpers/Maybe';
import {Action, UseCases} from '../../../types/Types';
import {SEARCH} from '../../../usecases/search/searchActions';
import {Query, QueryParameter} from '../../../usecases/search/searchModels';
import {resetReducer} from '../../domain-models/domainModelsReducer';
import {CHANGE_PAGE, UPDATE_PAGE_METADATA} from './paginationActions';
import {
  PaginationChangePayload,
  PaginationMetadataPayload,
  PaginationModel,
  PaginationState,
} from './paginationModels';

export const limit = 20;

export const initialPaginationModel: PaginationModel = {
  useCases: {},
  totalElements: -1,
  totalPages: -1,
  size: limit,
};

export const initialPaginationState: PaginationState = {
  meters: {...initialPaginationModel},
  gateways: {...initialPaginationModel},
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
  [entityType]: {useCases: {}, size: limit, ...state[entityType], totalElements, totalPages},
});

const hasQuery = ({query}: Query) => isDefined(query);

const resetGatewaysPage: PaginationChangePayload = {
  entityType: 'gateways',
  componentId: 'collectionGatewayList',
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
    .map(() => resetGatewaysPage)
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
    case SEARCH:
      return changePage(
        state,
        toPaginationChangePayload((action as Action<QueryParameter>).payload),
      );
    default:
      return resetReducer<PaginationState>(state, action, {...initialPaginationState});
  }
};
