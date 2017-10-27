import {AnyAction} from 'redux';
import {
  COLLECTION_CHANGE_PAGE,
  COLLECTION_REQUEST,
  COLLECTION_SUCCESS,
} from './collectionActions';
import {CollectionState} from './models/Collections';

const initialState: CollectionState = {
  title: 'CollectionState',
  isFetching: false,
  filter: {},
  categories: {
    handled: {
      total: 0,
      city: {
        count: 0,
        entities: [],
      },
      productModel: {
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
      productModel: {
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
    default:
      return state;
  }
};
