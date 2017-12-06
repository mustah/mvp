import {getCollectionPagination, getPaginationList, PaginatedDomainModel} from '../pagination/paginationSelectors';
import {UiState} from '../uiReducer';

describe('paginationSelectors', () => {

  it('can retreive the correct pagination metadata', () => {
    const fakedCollectionState: UiState = {
      tabs: {},
      indicator: {
        selectedIndicators: {},
      },
      sideMenu: {
        isOpen: false,
      },
      selectionTree: {
        openListItems: [],
      },
      pagination: {
        dashboard: {
          page: 1,
          limit: 2,
        },
        collection: {
          page: 3,
          limit: 4,
        },
        validation: {
          page: 5,
          limit: 6,
        },
        selection: {
          page: 7,
          limit: 8,
        },
      },
    };
    const collectionPagination = getCollectionPagination(fakedCollectionState);
    expect(collectionPagination).toEqual({limit: 4, page: 3});
  });

  it('can paginate the collection result, based on pagination metadata', () => {
    const paginationData: PaginatedDomainModel = {
      pagination: {
        page: 2,
        limit: 1,
      },
      result: [
        1,
        2,
      ],
    };

    expect(getPaginationList(paginationData)).toEqual([2]);
  });

  it('defaults to an empty result set if limit is 0', () => {
    const paginationData: PaginatedDomainModel = {
      pagination: {
        page: 1,
        limit: 0,
      },
      result: [
        1,
      ],
    };

    expect(getPaginationList(paginationData)).toEqual([]);
  });

  it('defaults to an empty result set if page is 0', () => {
    const paginationData: PaginatedDomainModel = {
      pagination: {
        page: 0,
        limit: 1,
      },
      result: [
        1,
      ],
    };

    expect(getPaginationList(paginationData)).toEqual([]);
  });

});
