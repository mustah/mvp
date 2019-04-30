import {Pagination, PaginationLookupState, PaginationState} from '../paginationModels';
import {getPagination} from '../paginationSelectors';

describe('paginationSelectors', () => {

  describe('getPagination', () => {
    const pagination: Pagination = {
      page: 0,
      size: 5,
      totalPages: 10,
      totalElements: 40,
    };
    const initialState: PaginationState = {
      meters: {...pagination, page: 5},
      gateways: {...pagination, page: 3},
      collectionStatFacilities: pagination,
      meterCollectionStatFacilities: pagination,
    };

    it('retrieves pagination from existing entityType', () => {
      const lookupState: PaginationLookupState = {
        pagination: initialState,
        entityType: 'meters',
      };

      const expected: Pagination = {...pagination, page: 5};

      expect(getPagination(lookupState)).toEqual(expected);
    });

    it('retrieves pagination for gateways', () => {
      const lookupState: PaginationLookupState = {
        pagination: initialState,
        entityType: 'gateways',
      };

      const expected: Pagination = {...pagination, page: 3};

      expect(getPagination(lookupState)).toEqual(expected);
    });
  });

});
