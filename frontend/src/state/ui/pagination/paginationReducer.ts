import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {PAGINATION_CHANGE_PAGE} from './paginationActions';
import {Pagination, PaginationState, SelectedPagination} from './paginationModels';

export const limit = 20;
export const initialComponentPagination: Pagination = {
  first: true,
  last: false,
  currentPage: 0,
  numberOfElements: -1,
  size: limit,
  sort: null,
  totalElements: -1,
  totalPages: -1,
};

type ActionTypes = Action<SelectedPagination> | EmptyAction<string>;

export const pagination = (state: PaginationState = {}, action: ActionTypes) => {
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
