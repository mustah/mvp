import configureStore, {MockStore} from 'redux-mock-store';
import {useCases} from '../../../../types/constants';
import {
  changePaginationCollection,
  changePaginationSelection,
  changePaginationValidation,
  PAGINATION_CHANGE_PAGE,
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
        type: PAGINATION_CHANGE_PAGE,
        payload: {
          page: 2,
          useCase: useCases.collection,
        },
      }]);
    });

    it('changes pagination for validation', () => {
      const action = changePaginationValidation(3);

      store.dispatch(action);

      expect(store.getActions()).toEqual([{
        type: PAGINATION_CHANGE_PAGE,
        payload: {
          page: 3,
          useCase: useCases.validation,
        },
      }]);
    });

    it('changes pagination for selection', () => {
      const action = changePaginationSelection(6);

      store.dispatch(action);

      expect(store.getActions()).toEqual([{
        type: PAGINATION_CHANGE_PAGE,
        payload: {
          page: 6,
          useCase: useCases.selection,
        },
      }]);
    });
  });

});
