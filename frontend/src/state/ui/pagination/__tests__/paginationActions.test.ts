import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {changePage, updatePageMetaData} from '../paginationActions';
import {PaginationChangePayload, PaginationMetadataPayload} from '../paginationModels';

const configureMockStore = configureStore([thunk]);
let store;

describe('paginationActions', () => {
  beforeEach(() => {
    store = configureMockStore({});
  });

  describe('changePage', () => {
    it('dispatches a requestPage action', () => {
      const payload: PaginationChangePayload = {entityType: 'meters', componentId: 'test', page: 2};

      store.dispatch(changePage(payload));

      expect(store.getActions()).toEqual([changePage(payload)]);
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

      store.dispatch(updatePageMetaData(payload));

      expect(store.getActions()).toEqual([updatePageMetaData(payload)]);
    });
  });
});
