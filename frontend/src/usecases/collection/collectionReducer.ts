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
  filter: {},
  categories: {
    handled: {allIds: [], byId: {}},
    unhandled: {allIds: [], byId: {}},
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
      return {
        ...state,
        gateways: action.payload,
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
