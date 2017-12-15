import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {PAGINATION_CHANGE_PAGE} from './paginationActions';
import {PaginationState, SelectedPagination} from './paginationModels';

export const limit = 30;

export const initialState: PaginationState = {
  collection: {page: 1, limit},
  validation: {page: 1, limit},
  selection: {page: 1, limit},
};

type ActionTypes = Action<SelectedPagination> | EmptyAction<string>;

export const pagination = (state: PaginationState = initialState, action: ActionTypes) => {
  switch (action.type) {
    case PAGINATION_CHANGE_PAGE:
      const {payload: {useCase, page}} = (action as Action<SelectedPagination>);
      return {
        ...state,
        [useCase]: {...state[useCase], page},
      };
    default:
      return state;
  }
};
