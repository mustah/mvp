import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {HasPageNumber, PaginatedDomainModelsState} from '../../domain-models-paginated/paginatedDomainModels';
import {DomainModelsState} from '../../domain-models/domainModels';
import {Pagination, PaginationLookupState, PaginationModel} from './paginationModels';
import {initialPaginationModel} from './paginationReducer';

export interface PaginatedDomainModel extends HasPageNumber {
  size: number;
  result: uuid[];
}

export const getPaginationList = createSelector<PaginatedDomainModel, uuid[], {size: number} & HasPageNumber, uuid[]>(
  ({result}: PaginatedDomainModel) => result,
  ({page, size}: PaginatedDomainModel) => ({page, size}),
  (result: uuid[], {page, size}: {size: number} & HasPageNumber) => result.slice((page - 1) * size, page * size),
);

type GetPagination = PaginationLookupState<PaginatedDomainModelsState & DomainModelsState>;

const isPaginationDefined = ({pagination, model}: GetPagination): boolean => !!pagination[model];
const isPageAssigned = ({pagination, model, componentId}: GetPagination): boolean =>
  isPaginationDefined({pagination, model, componentId}) && !!pagination[model]!.useCases[componentId];

const getMetadata = (state: GetPagination): PaginationModel => isPaginationDefined(state) ?
  state.pagination[state.model]! : {...initialPaginationModel};

const getPage = (state: GetPagination): HasPageNumber => isPageAssigned(state) ?
  state.pagination[state.model]!.useCases[state.componentId] : {page: 0};

export const getPagination = createSelector<GetPagination, PaginationModel, HasPageNumber, Pagination>(
  getMetadata,
  getPage,
  ({useCases, ...metadata}: PaginationModel, hasPage: HasPageNumber) => ({...metadata, ...hasPage}),
);
