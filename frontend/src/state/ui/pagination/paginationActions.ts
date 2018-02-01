import {createPayloadAction} from 'react-redux-typescript';
import {SelectedPagination} from './paginationModels';

export const PAGINATION_SET_PAGE = 'PAGINATION_SET_PAGE';

export const paginationSetPage = createPayloadAction<string, SelectedPagination>(PAGINATION_SET_PAGE);
