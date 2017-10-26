import {AnyAction} from 'redux';
import {FilterState} from './filterModels';

const initialState = {};

export const filter = (state: FilterState = initialState, action: AnyAction): FilterState => {
  // payload
  const COLLECTION_SET_FILTER = 'COLLECTION_SET_FILTER';

  switch (action.type) {
    case COLLECTION_SET_FILTER:
      return {
        ...state,
      };
    default:
      return state;
  }
};
