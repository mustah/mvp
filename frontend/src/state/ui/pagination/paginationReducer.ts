import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {PAGINATION_REQUEST_PAGE, PAGINATION_UPDATE_METADATA} from './paginationActions';
import {Pagination, PaginationState, PaginationMetadataPayload, PaginationChangePayload} from './paginationModels';

export const limit = 5;
export const initialComponentPagination: Pagination = {
  first: true,
  last: false,
  requestedPage: 0,
  currentPage: 0,
  numberOfElements: -1,
  size: limit,
  sort: null,
  totalElements: -1,
  totalPages: -1,
};

type ActionTypes = Action<PaginationMetadataPayload> | Action<PaginationChangePayload> | EmptyAction<string>;

const requestPage = (
  state: PaginationState,
  {payload: {componentId, page}}: Action<PaginationChangePayload>,
): PaginationState => ({
  ...state,
  [componentId]: {...state[componentId], requestedPage: page},
});

const updateMetaData = (
  state: PaginationState,
  {payload: {page, componentId}}: Action<PaginationMetadataPayload>,
): PaginationState => ({
  ...state,
  [componentId]: {...state[componentId], ...page},
});

export const pagination = (state: PaginationState = {}, action: ActionTypes) => {
  switch (action.type) {
    case PAGINATION_REQUEST_PAGE:
      return requestPage(state, action as Action<PaginationChangePayload>);
    case PAGINATION_UPDATE_METADATA:
      return updateMetaData(state, action as Action<PaginationMetadataPayload>);
    default:
      return state;
  }
};
