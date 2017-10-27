import {AnyAction} from 'redux';
import {PaginationState} from './paginationModels';
import {PAGINATION_CHANGE_PAGE} from './paginationActions';

const initialState: PaginationState = {
  dashboard: {page: 0, limit: 20},
  collection: {page: 0, limit: 20},
  validation: {page: 0, limit: 20},
  selection: {page: 0, limit: 20},
};

export const pagination = (state: PaginationState = initialState, action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case PAGINATION_CHANGE_PAGE:
      return {
        ...state,
        [payload.useCase]: {
          ...state[payload.useCase],
        },
      };
    default:
      return state;
  }
};
