import {createPayloadAction} from 'react-redux-typescript';
import {useCases} from '../../../types/constants';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

const paginationChangePage = createPayloadAction(PAGINATION_CHANGE_PAGE);

export const changePaginationCollection = (page: number) => paginationChangePage({
  page,
  useCase: useCases.collection,
});

export const changePaginationValidation = (page: number) => paginationChangePage({
  page,
  useCase: useCases.validation,
});

export const changePaginationSelection = (page: number) => paginationChangePage({
  page,
  useCase: useCases.selection,
});
