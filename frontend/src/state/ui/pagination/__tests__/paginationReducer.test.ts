import {paginationChangePage, paginationUpdateMetaData} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload, PaginationState} from '../paginationModels';
import {initialPaginationState, limit, pagination} from '../paginationReducer';

describe('paginationReducer', () => {

  const paginatedState: Readonly<PaginationState> = {
    meters: {size: limit, totalPages: 10, totalElements: 100, useCases: {component1: {page: 0}, component2: {page: 0}}},
    measurements: {size: limit, totalPages: 10, totalElements: 100, useCases: {}},
  };

  describe('pagination meta data', () => {

    it('has initial state', () => {
      expect(pagination(undefined, {type: 'unknown'})).toEqual({...initialPaginationState});
    });

    it('updates pagination meta data for a component', () => {
      const payload: PaginationMetadataPayload = {
        model: 'meters',
        content: [],
        first: false,
        last: false,
        number: 2,
        numberOfElements: 10,
        size: 10,
        sort: null,
        totalElements: 2000,
        totalPages: 200,
      };

      const expectedState: PaginationState = {
        ...initialPaginationState,
        meters: {
          ...initialPaginationState.meters,
          totalPages: payload.totalPages,
          totalElements: payload.totalElements,
          useCases: {},
        },
      };
      expect(pagination(undefined, paginationUpdateMetaData(payload))).toEqual(expectedState);
    });
  });

  describe('pagination change page', () => {
    const payload: PaginationChangePayload = {
      componentId: 'test',
      model: 'meters',
      page: 10,
    };

    it('changes requestedPage', () => {

      const expectedState: PaginationState = {
        ...initialPaginationState,
        meters: {...initialPaginationState.meters, useCases: {test: {page: 10}}},
      };

      expect(pagination(undefined, paginationChangePage(payload))).toEqual(expectedState);

    });

    it('only changes requestedPage for the targeted component', () => {
      const expectedState: PaginationState = {
        ...paginatedState,
        meters: {...paginatedState.meters, useCases: {...paginatedState.meters.useCases, test: {page: 10}}},
      };

      expect(pagination(paginatedState, paginationChangePage(payload))).toEqual(expectedState);

    });
  });
});
