import {AnyAction} from 'redux';
import {COLLECTION_REQUEST} from '../../types/ActionTypes';
import {CollectionState} from './models/Collections';

const initialState: CollectionState = {
  title: 'CollectionState',
  records: [],
  isFetching: false,
};

export const collection = (state: CollectionState = initialState, action: AnyAction): CollectionState => {
  switch (action.type) {
    case COLLECTION_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    default:
      return state;
  }
};
