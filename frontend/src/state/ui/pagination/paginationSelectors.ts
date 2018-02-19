import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {HasPageNumber, PaginatedDomainModelsState} from '../../domain-models-paginated/paginatedDomainModels';
import {DomainModelsState} from '../../domain-models/domainModels';
import {Pagination, PaginationLookupState, PaginationModel} from './paginationModels';
import {initialPaginationModel} from './paginationReducer';

export interface PaginatedDomainModel extends Pagination {
  result: uuid[];
}

export const getPaginationList = createSelector<PaginatedDomainModel, uuid[], {size: number} & HasPageNumber, uuid[]>(
  ({result}: PaginatedDomainModel) => result,
  ({page, size}: PaginatedDomainModel) => ({page, size}),
  (result: uuid[], {page, size}: {size: number} & HasPageNumber) => result.slice((page) * size, (page + 1) * size),
);

type GetPagination = PaginationLookupState<PaginatedDomainModelsState & DomainModelsState>;

const isPaginationDefined = ({pagination, entityType}: GetPagination): boolean => !!pagination[entityType];
const isPageAssigned = ({pagination, entityType, componentId}: GetPagination): boolean =>
  isPaginationDefined({pagination, entityType, componentId}) && !!pagination[entityType]!.useCases[componentId];

const getMetadata = (state: GetPagination): PaginationModel => isPaginationDefined(state) ?
  state.pagination[state.entityType]! : {...initialPaginationModel};

const getPage = (state: GetPagination): HasPageNumber => isPageAssigned(state) ?
  state.pagination[state.entityType]!.useCases[state.componentId] : {page: 0};

export const getPagination = createSelector<GetPagination, PaginationModel, HasPageNumber, Pagination>(
  getMetadata,
  getPage,
  ({useCases, ...metadata}: PaginationModel, hasPage: HasPageNumber) => ({...metadata, ...hasPage}),
);
