import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {Pagination} from '../../../usecases/collection/models/Collections';
import {Gateway, Gateways, GatewaysState} from './gatewayModels';

interface PaginatedGateways {
  gateways: Gateways;
  pagination: Pagination;
}

const getResult = (state: PaginatedGateways): uuid[] => state.gateways.result;
export const getGatewaysTotal = (state: GatewaysState): number => state.total;
export const getGatewayEntities = (state: GatewaysState): {[key: string]: Gateway} => state.entities.gateways;

const getPagination = (state: PaginatedGateways): Pagination => state.pagination;

export const getPaginationList = createSelector<PaginatedGateways, uuid[], Pagination, uuid[]>(
  getResult,
  getPagination,
  (result: uuid[], pagination: Pagination) => {
    const {page, limit} = pagination;
    return result.slice((page - 1) * limit, page * limit);
  });
