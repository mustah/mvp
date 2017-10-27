import {createPayloadAction} from 'react-redux-typescript';

export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

export const paginationChangePage = createPayloadAction(PAGINATION_CHANGE_PAGE);
