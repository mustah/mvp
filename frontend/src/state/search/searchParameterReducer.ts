import {combineReducers} from 'redux';
import {selection, SelectionState} from './selection/selectionReducer';

export interface SearchParameterState {
  selection: SelectionState;
}

export const searchParameters = combineReducers<SearchParameterState>({selection});
