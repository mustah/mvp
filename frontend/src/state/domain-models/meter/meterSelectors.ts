import {Meter, Meters, MetersState} from './meterModels';
import {Pagination} from '../../../usecases/ui/pagination/paginationModels';
import {uuid} from '../../../types/Types';
import {createSelector} from 'reselect';

interface PaginatedMeters {
  meters: Meters;
  pagination: Pagination;
}

const getResult = (state: PaginatedMeters): uuid[] => state.meters.result;
export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): {[key: string]: Meter} => state.entities.meters;

const getPagination = (state: PaginatedMeters): Pagination => state.pagination;

export const getPaginationList = createSelector<PaginatedMeters, uuid[], Pagination, uuid[]>(
  getResult,
  getPagination,
  (result: uuid[], pagination: Pagination) => {
    const {page, limit} = pagination;
    return result.slice((page - 1) * limit, page * limit);
  });
