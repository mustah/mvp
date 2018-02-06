import {createSelector, OutputSelector} from 'reselect';
import {UseCases, uuid} from '../../../types/Types';
import {UiState} from '../uiReducer';
import {PaginationMetadata, PaginationState} from './paginationModels';

export interface PaginatedDomainModel {
  pagination: PaginationMetadata;
  result: uuid[];
}

const getPaginationState = (state: UiState): PaginationState => state.pagination;

type UseCasePaginationSelector =
  OutputSelector<UiState, PaginationMetadata, (res: PaginationState) => PaginationMetadata>;

const getPaginationFor: (useCase: string) => UseCasePaginationSelector = (useCase: string) =>
  createSelector<UiState, PaginationState, PaginationMetadata>(
    getPaginationState,
    (pagination: PaginationState) => pagination[useCase],
  );

const getResult = (state: PaginatedDomainModel): uuid[] => state.result;
const getPagination = (state: PaginatedDomainModel): PaginationMetadata => state.pagination;

type PaginationListSelector =
  OutputSelector<PaginatedDomainModel, uuid[], (res1: uuid[], res2: PaginationMetadata) => uuid[]>;

export const getPaginationList: PaginationListSelector =
  createSelector<PaginatedDomainModel, uuid[], PaginationMetadata, uuid[]>(
    getResult,
    getPagination,
    (result: uuid[], {page, limit}: PaginationMetadata) => {
      return result.slice((page - 1) * limit, page * limit);
    });

export const getCollectionPagination = getPaginationFor(UseCases.collection);
export const getValidationPagination = getPaginationFor(UseCases.validation);
