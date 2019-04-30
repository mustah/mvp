import {createStandardAction} from 'typesafe-actions';
import {ChangePagePayload, PaginationMetadataPayload} from './paginationModels';

export const updatePageMetaData = createStandardAction('UPDATE_PAGE_METADATA')<PaginationMetadataPayload>();

export const changePage = createStandardAction('CHANGE_PAGE')<ChangePagePayload>();
