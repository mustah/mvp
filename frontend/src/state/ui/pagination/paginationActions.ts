import {payloadActionOf} from '../../../types/Types';
import {PaginationChangePayload, PaginationMetadataPayload} from './paginationModels';

export const PAGINATION_UPDATE_METADATA = 'PAGINATION_UPDATE_METADATA';
export const PAGINATION_CHANGE_PAGE = 'PAGINATION_CHANGE_PAGE';

export const paginationUpdateMetaData =
  payloadActionOf<PaginationMetadataPayload>(PAGINATION_UPDATE_METADATA);

export const changePaginationPage =
  payloadActionOf<PaginationChangePayload>(PAGINATION_CHANGE_PAGE);
