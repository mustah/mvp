import {createSelector} from 'reselect';
import {useCases} from '../../../types/constants';
import {uuid} from '../../../types/Types';
import {UiState} from '../uiReducer';
import {Pagination, PaginationState} from './paginationModels';

interface PaginatedDomainModel {
  pagination: Pagination;
  result: uuid[];
}

const getPaginationState = (state: UiState): PaginationState => state.pagination;

const getPaginationUseCase = (useCase: string): any =>
  createSelector<UiState, PaginationState, Pagination>(
    getPaginationState,
    (pagination: PaginationState) => pagination[useCase],
  );

const getResult = (state: PaginatedDomainModel): uuid[] => state.result;
const getPagination = (state: PaginatedDomainModel): Pagination => state.pagination;

export const getPaginationList = createSelector<PaginatedDomainModel, uuid[], Pagination, uuid[]>(
  getResult,
  getPagination,
  (result: uuid[], {page, limit}: Pagination) => {
    return result.slice((page - 1) * limit, page * limit);
  });

export const getCollectionPagination = getPaginationUseCase(useCases.collection);
export const getValidationPagination = getPaginationUseCase(useCases.validation);
export const getSelectionPagination = getPaginationUseCase(useCases.selection);
