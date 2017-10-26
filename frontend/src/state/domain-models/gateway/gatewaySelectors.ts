import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {Pagination} from '../../../usecases/collection/models/Collections';

interface PaginatedGateways {
  gateways: uuid[];
  pagination: Pagination;
}

const getResult = (state: PaginatedGateways): uuid[] => state.gateways;
const getPagination = (state: PaginatedGateways): Pagination => state.pagination;

export const getPaginationList = createSelector<PaginatedGateways, uuid[], Pagination, uuid[]>(
  getResult,
  getPagination,
  (result: uuid[], pagination: Pagination) => {
    const {page, limit} = pagination;
    return result.slice((page - 1) * limit, page * limit);
  });
