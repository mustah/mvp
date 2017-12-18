import {
  changePaginationCollection,
  changePaginationSelection,
  changePaginationValidation,
} from '../paginationActions';
import {initialState, limit, pagination} from '../paginationReducer';

describe('paginationReducer', () => {

  describe('pagination', () => {

    it('changes pagination for collection', () => {
      expect(pagination(initialState, changePaginationCollection(2))).toEqual({
        ...initialState,
        collection: {page: 2, limit},
      });
    });

    it('changes pagination for collection twice', () => {
      expect(pagination(initialState, changePaginationCollection(77))).toEqual({
        ...initialState,
        collection: {page: 77, limit},
      });

      expect(pagination(initialState, changePaginationCollection(48))).toEqual({
        ...initialState,
        collection: {page: 48, limit},
      });
    });

    it('changes pagination for selection', () => {
      expect(pagination(initialState, changePaginationSelection(123))).toEqual({
        ...initialState,
        selection: {page: 123, limit},
      });
    });

    it('changes pagination for validation', () => {
      expect(pagination(initialState, changePaginationValidation(789))).toEqual({
        ...initialState,
        validation: {page: 789, limit},
      });
    });
  });

});
