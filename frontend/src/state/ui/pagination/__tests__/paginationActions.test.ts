import configureStore, {MockStore} from 'redux-mock-store';
import {UseCases} from '../../../../types/Types';
import {
  changePaginationCollection,
  changePaginationSelection,
  changePaginationValidation,
  PAGINATION_SET_PAGE,
} from '../paginationActions';

describe('paginationActions', () => {

  let store: MockStore<any>;

  beforeEach(() => {
    store = configureStore()({});
  });

  describe('changePaginationAction', () => {

    it('changes pagination for collection', () => {
      const action = changePaginationCollection(2);

      store.dispatch(action);

      expect(store.getActions()).toEqual([{
        type: PAGINATION_SET_PAGE,
        payload: {
          page: 2,
          useCase: UseCases.collection,
        },
      }]);
    });

    it('changes pagination for validation', () => {
      const action = changePaginationValidation(3);

      store.dispatch(action);

      expect(store.getActions()).toEqual([{
        type: PAGINATION_SET_PAGE,
        payload: {
          page: 3,
          useCase: UseCases.validation,
        },
      }]);
    });

    it('changes pagination for selection', () => {
      const action = changePaginationSelection(6);

      store.dispatch(action);

      expect(store.getActions()).toEqual([{
        type: PAGINATION_SET_PAGE,
        payload: {
          page: 6,
          useCase: UseCases.selection,
        },
      }]);
    });
  });

});
