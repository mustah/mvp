import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {PaginationChangePayload, PaginationMetadataPayload} from './paginationModels';

export const PAGINATION_UPDATE_METADATA = 'PAGINATION_UPDATE_METADATA';
export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';
export const PAGINATION_RESET = 'PAGINATION_RESET';

export const paginationReset = createEmptyAction<string>(PAGINATION_RESET);

export const paginationUpdateMetaData =
  createPayloadAction<string, PaginationMetadataPayload>(PAGINATION_UPDATE_METADATA);

export const changePaginationPage = createPayloadAction<string, PaginationChangePayload>(PAGINATION_CHANGE_PAGE);
