import {createPayloadAction} from 'react-redux-typescript';
import {PaginationChangePayload, PaginationMetadataPayload} from './paginationModels';

export const PAGINATION_UPDATE_METADATA = 'PAGINATION_UPDATE_METADATA';
export const PAGINATION_REQUEST_PAGE = 'PAGINATION_REQUEST_PAGE';

export const paginationUpdateMetaData =
  createPayloadAction<string, PaginationMetadataPayload>(PAGINATION_UPDATE_METADATA);

export const paginationRequestPage = createPayloadAction<string, PaginationChangePayload>(PAGINATION_REQUEST_PAGE);
