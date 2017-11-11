import {AnyAction} from 'redux';
import {PAGINATION_CHANGE_PAGE} from './paginationActions';
import {PaginationState} from './paginationModels';

const limit = 30;

const initialState: PaginationState = {
  dashboard: {page: 1, limit},
  collection: {page: 1, limit},
  validation: {page: 1, limit},
  selection: {page: 1, limit},
};

export const pagination = (state: PaginationState = initialState, action: AnyAction) => {
  const {payload} = action;

  switch (action.type) {
    case PAGINATION_CHANGE_PAGE:
      return {
        ...state,
        [payload.useCase]: {
          ...state[payload.useCase],
          page: payload.page,
        },
      };
    default:
      return state;
  }
};
