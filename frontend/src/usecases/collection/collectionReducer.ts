import {AnyAction} from 'redux';
import {
  COLLECTION_ADD_FILTER,
  COLLECTION_REQUEST,
  COLLECTION_SET_FILTER,
  COLLECTION_SUCCESS,
} from './collectionActions';
import {CollectionState} from './models/Collections';

const initialState: CollectionState = {
  title: 'CollectionState',
  isFetching: false,
  pagination: {page: 1, limit: 20},
  filter: {},
  categories: {
    handled: {
      total: 0,
      city: {
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
      city: {
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
