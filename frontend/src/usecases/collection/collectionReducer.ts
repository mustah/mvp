import {AnyAction} from 'redux';
import {
  COLLECTION_ADD_FILTER,
  COLLECTION_REQUEST,
  COLLECTION_SET_FILTER,
  COLLECTION_SUCCESS,
  GATEWAY_REQUEST,
  GATEWAY_SUCCESS,
} from './collectionActions';
import {CollectionState} from './models/Collections';

const initialState: CollectionState = {
  title: 'CollectionState',
  records: [],
  isFetching: false,
  gateways: {allIds: [], byId: {}},
  pagination: {page: 1, limit: 20, total: 0},
  filter: {},
  categories: {
    handled: {
      total: 0,
      area: {
        count: 0,
        entities: [],
      },
      product_model: {
        count: 0,
        entities: [],
      },
    },
    unhandled: {
      total: 0,
      area: {
        count: 0,
        entities: [],
      },
      product_model: {
        count: 0,
        entities: [],
      },
    },
  },
};

export const collection = (state: CollectionState = initialState, action: AnyAction): CollectionState => {
  // TODO make sure to handle failed HTTP requests as well (*_FAILURE)
  switch (action.type) {
    case COLLECTION_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case COLLECTION_SUCCESS:
      return {
        ...state,
        categories: action.payload,
        isFetching: false,
      };
    case GATEWAY_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GATEWAY_SUCCESS:
      const {gateways, filter, page, limit, total} = action.payload;
      return {
        ...state,
        gateways,
        filter,
        pagination: {
          ...state.pagination,
          page,
          limit,
          total,
        },
        isFetching: false,
      };
    case COLLECTION_SET_FILTER:
      return {
        ...state,
        filter: {
          ...action.payload,
        },
      };
    case COLLECTION_ADD_FILTER:
      return {
        ...state,
        filter: {
          ...state.filter,
          ...action.payload,
        },
      };
    default:
      return state;
  }
};
