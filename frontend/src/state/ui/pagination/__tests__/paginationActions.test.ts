import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {
  PAGINATION_REQUEST_PAGE,
  PAGINATION_UPDATE_METADATA,
  paginationRequestPage,
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
    it('dipatches a requestPage action', () => {
      const payload: PaginationChangePayload = {model: 'meters', componentId: 'test', page: 2};

      store.dispatch(paginationRequestPage(payload));

      expect(store.getActions()).toEqual([
        {type: PAGINATION_REQUEST_PAGE, payload},
      ]);
    });

    it('dispatches a update metadata request', () => {
      const payload: PaginationMetadataPayload = {
        model: 'meters',
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
  });
});
