import {AnyAction} from 'redux';
import {CollectionState} from './models/Collections';
import {
  COLLECTION_REQUEST, COLLECTION_SET_FILTER, COLLECTION_SUCCESS, GATEWAY_REQUEST,
  GATEWAY_SUCCESS,
} from './collectionActions';

const initialState: CollectionState = {
  title: 'CollectionState',
  records: [],
  isFetching: false,
  gateways: {allIds: [], byId: {}},
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
      console.log('SetFilter: ', action.payload);
      return state; // TODO: Update to set a filter in state.
    default:
      return state;
  }
};
