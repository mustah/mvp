import {RequestParameter} from '../../../../helpers/urlFactory';
import {
  sortCollectionStats,
  sortMeterCollectionStats,
  sortMeters
} from '../../../domain-models-paginated/paginatedDomainModelsActions';
import {search} from '../../../search/searchActions';
import {makeMeterQuery} from '../../../search/searchModels';
import {resetSelection} from '../../../user-selection/userSelectionActions';
import {changePage, updatePageMetaData} from '../paginationActions';
import {ChangePagePayload, PaginationMetadataPayload, PaginationState, SortOption} from '../paginationModels';
import {initialState, pagination, paginationPageSize} from '../paginationReducer';

describe('paginationReducer', () => {

  const initialPagination = {
    page: 0,
    size: paginationPageSize,
    totalPages: 10,
    totalElements: 100,
  };

  const paginatedState: Readonly<PaginationState> = {
    batchReferences: initialPagination,
    meters: initialPagination,
    gateways: initialPagination,
    collectionStatFacilities: initialPagination,
    meterCollectionStatFacilities: initialPagination,
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
        },
      };
      expect(pagination(undefined, updatePageMetaData(payload))).toEqual(expectedState);
    });
  });

  it('updates pagination but leaves useCases intact', () => {
    const paginatedState: PaginationState = {
      ...initialState,
      meters: {...initialState.meters, page: 1},
    };
    const payload: PaginationMetadataPayload = {
      entityType: 'meters',
      content: [],
      totalElements: 2000,
      totalPages: 200,
    };

    const expectedPaginatedState: PaginationState = {
      ...initialState,
      meters: {
        page: 1,
        size: paginationPageSize,
        totalPages: 200,
        totalElements: 2000,
      },
    };

    expect(pagination(paginatedState, updatePageMetaData(payload))).toEqual(expectedPaginatedState);

  });

  describe('pagination change page', () => {
    const payload: ChangePagePayload = {entityType: 'meters', page: 10};

    it('changes requestedPage', () => {
      const expectedState: PaginationState = {
        ...initialState,
        meters: {...initialState.meters, page: 10},
      };

      expect(pagination(undefined, changePage(payload))).toEqual(expectedState);
    });

    it('only changes requested page for the targeted component', () => {
      const expectedState: PaginationState = {
        ...paginatedState,
        meters: {
          ...paginatedState.meters,
          page: 10,
        },
      };

      expect(pagination(paginatedState, changePage(payload))).toEqual(expectedState);
    });

    it('resets meters pagination state when meters search is performed', () => {
      const expectedState: PaginationState = {...initialState, gateways: paginatedState.gateways};

      expect(pagination(paginatedState, search(makeMeterQuery('ok')))).toEqual(expectedState);
    });

    it('does not change page when there is nothing to search for', () => {
      expect(pagination(paginatedState, search(makeMeterQuery(undefined)))).toBe(paginatedState);
    });
  });

  describe('reset pagination', () => {
    const paginatedState: PaginationState = {
      ...initialState,
      meters: {
        size: paginationPageSize,
        totalPages: 1,
        totalElements: 1,
        page: 1,
      },
    };

    it('sets pagination to initial state when selection is reset', () => {
      expect(pagination(paginatedState, resetSelection())).toBe(initialState);
    });

    it('set pagination to initial state when meter table is sorted', () => {
      const payload: SortOption[] = [{field: RequestParameter.facility, dir: 'ASC'}];

      expect(pagination(paginatedState, sortMeters(payload))).toEqual(initialState);
    });
  });

  describe('reset state for collections stats', () => {
    const paginatedState: PaginationState = {
      ...initialState,
      collectionStatFacilities: {
        size: paginationPageSize,
        totalPages: 1,
        totalElements: 1,
        page: 1,
      },
    };

    it('sets pagination to initial state when selection is reset', () => {
      expect(pagination(paginatedState, resetSelection())).toBe(initialState);
    });

    it('set pagination to initial state when meter table is sorted', () => {
      const payload: SortOption[] = [{field: RequestParameter.facility, dir: 'ASC'}];

      expect(pagination(paginatedState, sortCollectionStats(payload))).toEqual(initialState);
    });
  });

  describe('reset state for collections stats', () => {
    const paginatedState: PaginationState = {
      ...initialState,
      meterCollectionStatFacilities: {
        size: paginationPageSize,
        totalPages: 1,
        totalElements: 1,
        page: 1,
      },
    };

    it('sets pagination to initial state when selection is reset', () => {
      expect(pagination(paginatedState, resetSelection())).toBe(initialState);
    });

    it('set pagination to initial state when meter table is sorted', () => {
      const payload: SortOption[] = [{field: RequestParameter.facility, dir: 'ASC'}];

      expect(pagination(paginatedState, sortMeterCollectionStats(payload))).toEqual(initialState);
    });
  });
});
