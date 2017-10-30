import {createPayloadAction} from 'react-redux-typescript';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

const paginationChangePage = createPayloadAction(PAGINATION_CHANGE_PAGE);

const DASHBOARD = 'dashboard';
const COLLECTION = 'collection';
const VALIDATION = 'validation';
const SELECTION = 'selection';

export const changePaginationDashboard = (page: number) => {
  paginationChangePage({
    page,
    useCase: DASHBOARD,
  });
};

export const changePaginationCollection = (page: number) => {
  paginationChangePage({
    page,
    useCase: COLLECTION,
  });
};

export const changePaginationValidation = (page: number) => {
  paginationChangePage({
    page,
    useCase: VALIDATION,
  });
};
export const changePaginationSelection = (page: number) => {
  paginationChangePage({
    page,
    useCase: SELECTION,
  });
};
