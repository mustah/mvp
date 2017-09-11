import {AnyAction} from 'redux';
import {collection as initialState} from '../../store/initialAppState';
import {COLLECTION_REQUEST} from '../../types/ActionTypes';
import {CollectionState} from './models/Collections';

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
