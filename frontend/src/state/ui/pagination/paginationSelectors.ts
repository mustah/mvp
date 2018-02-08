import {createSelector, OutputSelector} from 'reselect';
import {UseCases, uuid} from '../../../types/Types';
import {UriLookupStatePaginated} from '../../search/selection/selectionSelectors';
import {UiState} from '../uiReducer';
import {Pagination, PaginationMetadata, PaginationState} from './paginationModels';
import {initialComponentPagination} from './paginationReducer';

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
const getPaginationObsolete = (state: PaginatedDomainModel): PaginationMetadata => state.pagination;

type PaginationListSelector =
  OutputSelector<PaginatedDomainModel, uuid[], (res1: uuid[], res2: PaginationMetadata) => uuid[]>;

export const getPaginationList: PaginationListSelector =
  createSelector<PaginatedDomainModel, uuid[], PaginationMetadata, uuid[]>(
    getResult,
    getPaginationObsolete,
    (result: uuid[], {page, limit}: PaginationMetadata) => {
      return result.slice((page - 1) * limit, page * limit);
    });

export const getCollectionPagination = getPaginationFor(UseCases.collection);

export const getPagination = ({model, componentId, pagination}: UriLookupStatePaginated): Pagination => {
  const {useCases, ...metaData} = pagination[model];
  return useCases[componentId] ? {...metaData, ...useCases[componentId]} : {...metaData, ...initialComponentPagination};
};
