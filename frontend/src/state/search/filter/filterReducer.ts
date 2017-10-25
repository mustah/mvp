import {AnyAction} from 'redux';
import {COLLECTION_SET_FILTER} from '../../../usecases/collection/collectionActions';
import {FilterState} from './filterModels';

const initialState = {};

export const filter = (state: FilterState = initialState, action: AnyAction): FilterState => {
  // payload
  switch (action.type) {
    case COLLECTION_SET_FILTER:
      return {
        ...state,
      };
    default:
      return state;
  }
};
