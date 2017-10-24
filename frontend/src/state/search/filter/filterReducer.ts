import {AnyAction} from 'redux';
import {FilterState} from './filterModels';

const initialState = {};

export const filter = (state: FilterState = initialState, action: AnyAction): FilterState => {
  return state;
};
