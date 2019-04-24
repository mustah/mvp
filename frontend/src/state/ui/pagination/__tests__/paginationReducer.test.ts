import {search} from '../../../search/searchActions';
import {makeMeterQuery} from '../../../search/searchModels';
import {resetSelection} from '../../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload, PaginationState} from '../paginationModels';
import {initialState, pagination, paginationPageSize} from '../paginationReducer';

describe('paginationReducer', () => {

  const paginatedState: Readonly<PaginationState> = {
    meters: {
      size: paginationPageSize,
      totalPages: 10,
      totalElements: 100,
      useCases: {component1: {page: 0}, component2: {page: 0}},
    },
    gateways: {
      size: paginationPageSize,
      totalPages: 10,
      totalElements: 100,
      useCases: {component1: {page: 0}, component2: {page: 0}},
    },
    collectionStatFacilities: {
      size: paginationPageSize,
      totalPages: 10,
      totalElements: 100,
      useCases: {component1: {page: 0}, component2: {page: 0}},
    },
    meterCollectionStatFacilities: {
      size: paginationPageSize,
      totalPages: 10,
      totalElements: 100,
      useCases: {component1: {page: 0}, component2: {page: 0}},
    },
  };

  describe('pagination meta data', () => {

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
        ...initialState,
        meters: {
          ...initialState.meters,
          totalPages: payload.totalPages,
          totalElements: payload.totalElements,
          useCases: {},
        },
      };
      expect(pagination(undefined, updatePageMetaData(payload))).toEqual(expectedState);
    });
  });
  it('updates pagination but leaves useCases intact', () => {
    const paginatedState: PaginationState = {
      meters: {size: paginationPageSize, totalPages: 1, totalElements: 1, useCases: {validationList: {page: 1}}},
      gateways: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
      collectionStatFacilities: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
      meterCollectionStatFacilities: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
    };
    const payload: PaginationMetadataPayload = {
      entityType: 'meters',
      content: [],
      totalElements: 2000,
      totalPages: 200,
    };

    const expectedPaginatedState: PaginationState = {
      meters: {
        size: paginationPageSize,
        totalPages: 200,
        totalElements: 2000,
        useCases: {validationList: {page: 1}},
      },
      gateways: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
      collectionStatFacilities: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
      meterCollectionStatFacilities: {size: paginationPageSize, totalPages: -1, totalElements: -1, useCases: {}},
    };

    expect(pagination(paginatedState, updatePageMetaData(payload))).toEqual(expectedPaginatedState);

  });

  describe('pagination change page', () => {
    const payload: PaginationChangePayload = {
      componentId: 'test',
      entityType: 'meters',
      page: 10,
    };

    it('changes requestedPage', () => {
      const expectedState: PaginationState = {
        ...initialState,
        meters: {...initialState.meters, useCases: {test: {page: 10}}},
      };

      expect(pagination(undefined, changePage(payload))).toEqual(expectedState);
    });

    it('only changes requestedPage for the targeted component', () => {
      const expectedState: PaginationState = {
        ...paginatedState,
        meters: {
          ...paginatedState.meters,
          useCases: {...paginatedState.meters.useCases, test: {page: 10}},
        },
      };

      expect(pagination(paginatedState, changePage(payload))).toEqual(expectedState);
    });

    it('changes page when wildcard search is performed', () => {
      const expectedState: PaginationState = {
        ...paginatedState,
        meters: {
          ...paginatedState.meters,
          useCases: {
            ...paginatedState.meters.useCases,
            validationMeterList: {page: 0},
          },
        },
      };
      expect(pagination(paginatedState, search(makeMeterQuery('ok')))).toEqual(expectedState);
    });
  });

  describe('reset pagination', () => {
    it('sets pagination to initialState when getting the reset action', () => {
      const paginatedState: PaginationState = {
        meters: {
          size: paginationPageSize,
          totalPages: 1,
          totalElements: 1,
          useCases: {validationList: {page: 1}},
        },
        gateways: {size: paginationPageSize, totalPages: 10, totalElements: 10, useCases: {}},
        collectionStatFacilities: {size: paginationPageSize, totalPages: 10, totalElements: 10, useCases: {}},
        meterCollectionStatFacilities: {size: paginationPageSize, totalPages: 10, totalElements: 10, useCases: {}},
      };

      expect(pagination(paginatedState, resetSelection())).toEqual(initialState);
    });
  });
});
