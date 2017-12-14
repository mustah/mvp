import {createPayloadAction} from 'react-redux-typescript';
import {Action, UseCases} from '../../../types/Types';
import {SelectedPagination} from './paginationModels';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

const paginationChangePage = createPayloadAction<string, SelectedPagination>(PAGINATION_CHANGE_PAGE);

export const changePaginationCollection = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: UseCases.collection,
  });

export const changePaginationValidation = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: UseCases.validation,
  });

export const changePaginationSelection = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: UseCases.selection,
  });
