import {mockSelectionAction} from '../../../../__tests__/testActions';
import {changePaginationPage, paginationUpdateMetaData} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload, PaginationState} from '../paginationModels';
import {initialPaginationState, limit, pagination} from '../paginationReducer';

describe('paginationReducer', () => {

  const paginatedState: Readonly<PaginationState> = {
    meters: {size: limit, totalPages: 10, totalElements: 100, useCases: {component1: {page: 0}, component2: {page: 0}}},
    gateways: {
      size: limit,
      totalPages: 10,
      totalElements: 100,
      useCases: {component1: {page: 0}, component2: {page: 0}},
    },
  };

  describe('pagination meta data', () => {

    it('has initial state', () => {
      expect(pagination(undefined, {type: 'unknown'})).toEqual({...initialPaginationState});
    });

    it('updates pagination meta data for a component', () => {
      const payload: PaginationMetadataPayload = {
        entityType: 'meters',
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
  it('updates pagination but leaves useCases intact', () => {
    const paginatedState: PaginationState = {
      meters: {size: limit, totalPages: 1, totalElements: 1, useCases: {validationList: {page: 1}}},
      gateways: {size: limit, totalPages: -1, totalElements: -1, useCases: {}},
    };
    const payload: PaginationMetadataPayload = {
      entityType: 'meters',
      content: [],
      totalElements: 2000,
      totalPages: 200,
    };

    expect(pagination(paginatedState, paginationUpdateMetaData(payload))).toEqual({
      meters: {size: limit, totalPages: 200, totalElements: 2000, useCases: {validationList: {page: 1}}},
      gateways: {size: limit, totalPages: -1, totalElements: -1, useCases: {}},
    });

  });

  describe('pagination change page', () => {
    const payload: PaginationChangePayload = {
      componentId: 'test',
      entityType: 'meters',
      page: 10,
    };

    it('changes requestedPage', () => {

      const expectedState: PaginationState = {
        ...initialPaginationState,
        meters: {...initialPaginationState.meters, useCases: {test: {page: 10}}},
      };

      expect(pagination(undefined, changePaginationPage(payload))).toEqual(expectedState);

    });

    it('only changes requestedPage for the targeted component', () => {
      const expectedState: PaginationState = {
        ...paginatedState,
        meters: {...paginatedState.meters, useCases: {...paginatedState.meters.useCases, test: {page: 10}}},
      };

      expect(pagination(paginatedState, changePaginationPage(payload))).toEqual(expectedState);

    });
  });
  describe('reset pagination', () => {
    it('sets pagination to initialState when getting the reset action', () => {
      const paginatedState: PaginationState = {
        meters: {size: limit, totalPages: 1, totalElements: 1, useCases: {validationList: {page: 1}}},
        gateways: {size: limit, totalPages: 10, totalElements: 10, useCases: {}},
      };

      expect(pagination(
        paginatedState,
        mockSelectionAction,
      )).toEqual({
        ...initialPaginationState,
      });
    });
  });
});
