import {Pagination, PaginationState} from './paginationModels';
import {uuid} from '../../../types/Types';
import {createSelector} from 'reselect';
import {DomainModel, getResultDomainModels} from '../../domain-models/domainModelsSelectors';
import {UiState} from '../uiReducer';

type PaginatedDomainModel = DomainModel & Pagination;

const getPaginationState = (state: UiState): PaginationState => state.pagination;

const getPagination = (useCase: string): any =>
  createSelector<UiState, PaginationState, Pagination> (
    getPaginationState,
    (pagination: PaginationState) => pagination[useCase],
  );

export const getPaginationList = createSelector<PaginatedDomainModel, uuid[], Pagination, uuid[]>(
  getResultDomainModels,
  ({page, limit}: PaginatedDomainModel) => ({page, limit}),
  (result: uuid[], pagination: Pagination) => {
    const {page, limit} = pagination;
    return result.slice((page - 1) * limit, page * limit);
  });

export const getCollectionPagination = getPagination('collection');
export const getValidationPagination = getPagination('validation');
export const getDashboardPagination = getPagination('dashboard');
export const getSelectionPagination = getPagination('selection');
