import {createPayloadAction} from 'react-redux-typescript';
import {useCases} from '../../../types/constants';
import {Action} from '../../../types/Types';
import {SelectedPagination} from './paginationModels';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

const paginationChangePage = createPayloadAction<string, SelectedPagination>(PAGINATION_CHANGE_PAGE);

export const changePaginationCollection = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: useCases.collection,
  });

export const changePaginationValidation = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: useCases.validation,
  });

export const changePaginationSelection = (page: number): Action<SelectedPagination> =>
  paginationChangePage({
    page,
    useCase: useCases.selection,
  });
