import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {PAGINATION_CHANGE_PAGE, PAGINATION_UPDATE_METADATA} from './paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload, PaginationModel, PaginationState} from './paginationModels';

export const limit = 5;

export const initialPaginationModel: PaginationModel = {
  useCases: {},
  totalElements: -1,
  totalPages: -1,
  size: limit,
};

export const initialPaginationState: PaginationState = {
  meters: {...initialPaginationModel},
  measurements: {...initialPaginationModel},
};

type ActionTypes = Action<PaginationMetadataPayload> | Action<PaginationChangePayload> | EmptyAction<string>;

const requestPage = (
  state: PaginationState,
  {payload: {model, componentId, page}}: Action<PaginationChangePayload>,
): PaginationState => ({
  ...state,
  [model]: {...state[model], useCases: {...state[model]!.useCases, [componentId]: {page}}},
});

const updateMetaData = (
  state: PaginationState,
  {payload: {model, totalElements, totalPages}}: Action<PaginationMetadataPayload>,
): PaginationState => ({
  ...state,
  [model]: {...state[model], totalElements, totalPages},
});

export const pagination = (state: PaginationState = initialPaginationState, action: ActionTypes) => {
  switch (action.type) {
    case PAGINATION_CHANGE_PAGE:
      return requestPage(state, action as Action<PaginationChangePayload>);
    case PAGINATION_UPDATE_METADATA:
      return updateMetaData(state, action as Action<PaginationMetadataPayload>);
    default:
      return state;
  }
};
