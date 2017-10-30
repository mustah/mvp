import {createPayloadAction} from 'react-redux-typescript';
import {COLLECTION, DASHBOARD, SELECTION, VALIDATION} from '../../../types/constants';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

const paginationChangePage = createPayloadAction(PAGINATION_CHANGE_PAGE);

export const changePaginationDashboard = (page: number) => paginationChangePage({
  page,
  useCase: DASHBOARD,
});

export const changePaginationCollection = (page: number) => paginationChangePage({
  page,
  useCase: COLLECTION,
});

export const changePaginationValidation = (page: number) => paginationChangePage({
  page,
  useCase: VALIDATION,
});

export const changePaginationSelection = (page: number) => paginationChangePage({
  page,
  useCase: SELECTION,
});
