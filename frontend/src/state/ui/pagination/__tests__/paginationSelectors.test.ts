import {DomainModelsState} from '../../../domain-models/domainModels';
import {Pagination, PaginationLookupState, PaginationState} from '../paginationModels';
import {paginationPageSize} from '../paginationReducer';
import {getPagination, getPaginationList, PaginatedDomainModel} from '../paginationSelectors';

describe('paginationSelectors', () => {

  describe('getPagination', () => {
    const initialState: PaginationState = {
      meters: {
        useCases: {
          meterList: {
            page: 5,
          },
        },
        size: 4,
        totalPages: 10,
        totalElements: 40,
      },
      gateways: {
        useCases: {
          gatewayList: {
            page: 3,
          },
        },
        size: 4,
        totalPages: 10,
        totalElements: 40,
      },
      meterMapMarkers: {
        useCases: {
          aList: {
            page: 3,
          },
        },
        size: 4,
        totalPages: 10,
        totalElements: 40,
      },
      collectionStatFacilities: {
        useCases: {
          collectionStatList: {
            page: 3,
          },
        },
        size: 4,
        totalPages: 10,
        totalElements: 40,
      },
    };

    it('retrieves pagination from existing entityType and componentId', () => {
      const lookupState: PaginationLookupState<DomainModelsState> = {
        pagination: initialState,
        componentId: 'aList',
        entityType: 'meterMapMarkers',
      };

      const expected: Pagination = {
        page: 3,
        size: 4,
        totalElements: 40,
        totalPages: 10,
      };

      expect(getPagination(lookupState)).toEqual(expected);
    });

    it('retrieves pagination from existing entityType but non-existing componentId', () => {
      const lookupState: PaginationLookupState<DomainModelsState> = {
        pagination: initialState,
        componentId: 'dont exist',
        entityType: 'meterMapMarkers',
      };

      const expected: Pagination = {
        page: 0,
        size: 4,
        totalElements: 40,
        totalPages: 10,
      };

      expect(getPagination(lookupState)).toEqual(expected);
    });

    it('retrieves pagination from non-existing entityType', () => {
      const lookupState: PaginationLookupState<DomainModelsState> = {
        pagination: initialState,
        componentId: 'dont exist',
        entityType: 'users',
      };

      const expected: Pagination = {
        page: 0,
        size: paginationPageSize,
        totalElements: -1,
        totalPages: -1,
      };

      expect(getPagination(lookupState)).toEqual(expected);
    });
  });

  describe('getPaginationList', () => {

    it('can paginate the collection result, based on pagination metadata', () => {
      const paginationData: PaginatedDomainModel = {
        page: 0,
        size: 1,
        totalPages: 10,
        totalElements: 100,
        result: [1, 2],
      };

      expect(getPaginationList(paginationData)).toEqual([1]);
    });

    it('defaults to an empty result set if limit is 0', () => {
      const paginationData: PaginatedDomainModel = {
        page: 0,
        size: 0,
        totalPages: 10,
        totalElements: 100,
        result: [1],
      };

      expect(getPaginationList(paginationData)).toEqual([]);
    });

    it('defaults to an empty result set if page is set to negative', () => {
      const paginationData: PaginatedDomainModel = {
        page: -1,
        size: 1,
        totalPages: 10,
        totalElements: 100,
        result: [1],
      };

      expect(getPaginationList(paginationData)).toEqual([]);
    });
    it('defaults to an empty result set if page is set over the number of avaliable pages', () => {
      const paginationData: PaginatedDomainModel = {
        page: 1,
        size: 1,
        totalPages: 10,
        totalElements: 100,
        result: [1],
      };

      expect(getPaginationList(paginationData)).toEqual([]);
    });
  });
});
