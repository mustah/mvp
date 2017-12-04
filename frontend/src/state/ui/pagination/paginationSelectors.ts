import {createSelector, OutputSelector} from 'reselect';
import {useCases} from '../../../types/constants';
import {uuid} from '../../../types/Types';
import {UiState} from '../uiReducer';
import {Pagination, PaginationState} from './paginationModels';

export interface PaginatedDomainModel {
  pagination: Pagination;
  result: uuid[];
}

const getPaginationState = (state: UiState): PaginationState => state.pagination;

type UseCasePaginationSelector = OutputSelector<UiState, Pagination, (res: PaginationState) => Pagination>;
const getPaginationFor: (useCase: string) => UseCasePaginationSelector = (useCase: string) =>
  createSelector<UiState, PaginationState, Pagination>(
    getPaginationState,
    (pagination: PaginationState) => pagination[useCase],
  );

const getResult = (state: PaginatedDomainModel): uuid[] => state.result;
const getPagination = (state: PaginatedDomainModel): Pagination => state.pagination;

type PaginationListSelector = OutputSelector<PaginatedDomainModel, uuid[], (res1: uuid[], res2: Pagination) => uuid[]>;
export const getPaginationList: PaginationListSelector =
  createSelector<PaginatedDomainModel, uuid[], Pagination, uuid[]>(
    getResult,
    getPagination,
    (result: uuid[], {page, limit}: Pagination) => {
      return result.slice((page - 1) * limit, page * limit);
    });

export const getCollectionPagination: UseCasePaginationSelector = getPaginationFor(useCases.collection);
export const getValidationPagination: UseCasePaginationSelector = getPaginationFor(useCases.validation);
export const getSelectionPagination: UseCasePaginationSelector = getPaginationFor(useCases.selection);
