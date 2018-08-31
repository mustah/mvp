import {payloadActionOf} from '../../../types/Types';
import {PaginationChangePayload, PaginationMetadataPayload} from './paginationModels';

export const UPDATE_PAGE_METADATA = 'UPDATE_PAGE_METADATA';
export const CHANGE_PAGE = 'CHANGE_PAGE';

export const updatePageMetaData =
  payloadActionOf<PaginationMetadataPayload>(UPDATE_PAGE_METADATA);

export const changePage =
  payloadActionOf<PaginationChangePayload>(CHANGE_PAGE);
