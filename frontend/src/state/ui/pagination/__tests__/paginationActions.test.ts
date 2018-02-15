import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {
  PAGINATION_CHANGE_PAGE, PAGINATION_RESET,
  PAGINATION_UPDATE_METADATA,
  changePaginationPage, paginationReset,
  paginationUpdateMetaData,
} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload} from '../paginationModels';

const configureMockStore = configureStore([thunk]);
let store;

describe('paginationActions', () => {
  beforeEach(() => {
    store = configureMockStore({});
  });

  describe('changePaginationAction', () => {
    it('dispatches a requestPage action', () => {
      const payload: PaginationChangePayload = {entityType: 'meters', componentId: 'test', page: 2};

      store.dispatch(changePaginationPage(payload));

      expect(store.getActions()).toEqual([
        {type: PAGINATION_CHANGE_PAGE, payload},
      ]);
    });

    it('dispatches a update metadata request', () => {
      const payload: PaginationMetadataPayload = {
        entityType: 'meters',
        content: [
          1,
          2,
        ],
        totalPages: 1440,
        totalElements: 28800,
        last: false,
        size: 2,
        number: 0,
        first: true,
        numberOfElements: 2,
        sort: null,
      };

      store.dispatch(paginationUpdateMetaData(payload));

      expect(store.getActions()).toEqual([
        {type: PAGINATION_UPDATE_METADATA, payload},
      ]);
    });
    it('dispatches a clear pagination action', () => {
      store.dispatch(paginationReset());

      expect(store.getActions()).toEqual([
        {type: PAGINATION_RESET},
      ]);
    });
  });
});
