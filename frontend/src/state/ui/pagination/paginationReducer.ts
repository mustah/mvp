import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {resetReducer} from '../../domain-models/domainModelsReducer';
import {PAGINATION_CHANGE_PAGE, PAGINATION_UPDATE_METADATA} from './paginationActions';
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
  Action<PaginationMetadataPayload>
  | Action<PaginationChangePayload>
  | EmptyAction<string>;

const requestPage = (
  state: PaginationState,
  {payload: {entityType, componentId, page}}: Action<PaginationChangePayload>,
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

export const pagination = (
  state: PaginationState = initialPaginationState,
  action: ActionTypes,
) => {
  switch (action.type) {
    case PAGINATION_CHANGE_PAGE:
      return requestPage(state, action as Action<PaginationChangePayload>);
    case PAGINATION_UPDATE_METADATA:
      return updateMetaData(state, action as Action<PaginationMetadataPayload>);
    default:
      return resetReducer<PaginationState>(state, action, {...initialPaginationState});
  }
};
