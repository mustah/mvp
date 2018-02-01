import {paginationSetPage} from '../paginationActions';
import {Pagination, PaginationState, SelectedPagination} from '../paginationModels';
import {limit, pagination} from '../paginationReducer';

describe('paginationReducer', () => {

  describe('pagination', () => {

    const paginatedState: Readonly<PaginationState> = {
      test: {
        first: false,
        last: false,
        currentPage: 4,
        numberOfElements: 10,
        size: limit,
        sort: null,
        totalElements: 200,
        totalPages: 20,
      },
    };
    const paginationData: Readonly<Pagination> = {
      first: false,
      last: false,
      currentPage: 2,
      numberOfElements: 10,
      size: limit,
      sort: null,
      totalElements: 200,
      totalPages: 20,
    };

    it('has initial state', () => {
      expect(pagination({}, {type: 'unknown'})).toEqual({});
    });

    it('changes pagination for a component', () => {
      const payload: SelectedPagination = {page: paginationData, componentId: 'test'};

      expect(pagination(paginatedState, paginationSetPage(payload))).toEqual({
        ...paginatedState,
        test: {...paginationData},
      });
    });
    it('only updates the targeted component and leave the others untouched', () => {
      const payload: SelectedPagination = {page: paginationData, componentId: 'test2'};

      expect(pagination(paginatedState, paginationSetPage(payload))).toEqual({
        ...paginatedState,
        test2: {...paginationData},
      });
    });
  });
});
