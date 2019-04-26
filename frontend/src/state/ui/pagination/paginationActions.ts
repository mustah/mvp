import {createStandardAction} from 'typesafe-actions';
import {PaginationChangePayload, PaginationMetadataPayload} from './paginationModels';

export const updatePageMetaData = createStandardAction('UPDATE_PAGE_METADATA')<PaginationMetadataPayload>();

export const changePage = createStandardAction('CHANGE_PAGE')<PaginationChangePayload>();
