import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {
  PAGINATION_REQUEST_PAGE, PAGINATION_UPDATE_METADATA, paginationRequestPage,
  paginationUpdateMetaData
} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload} from '../paginationModels';
import {initialComponentPagination} from '../paginationReducer';

const configureMockStore = configureStore([thunk]);
let store;

describe('paginationActions', () => {
  beforeEach(() => {
    store = configureMockStore({});
  });

  describe('changePaginationAction', () => {
    it('dipatches a requestPage action', () => {
      const payload: PaginationChangePayload = {componentId: 'test', page: 2};

      store.dispatch(paginationRequestPage(payload));

      expect(store.getActions()).toEqual([
        {type: PAGINATION_REQUEST_PAGE, payload},
      ]);
    });

    it('dispatches a update metadata request', () => {
      const payload: PaginationMetadataPayload = {
        componentId: 'test', page: {...initialComponentPagination},
      };

      store.dispatch(paginationUpdateMetaData(payload));

      expect(store.getActions()).toEqual([
        {type: PAGINATION_UPDATE_METADATA, payload},
      ]);
    });
  });
});
