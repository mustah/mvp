import {combineReducers} from 'redux';
import {SelectionState} from './selection/selectionModels';
import {selection} from './selection/selectionReducer';
import {saved} from './selection/saveReducer';

export interface SearchParameterState {
  selection: SelectionState;
  saved: SelectionState[];
}

export const searchParameters = combineReducers<SearchParameterState>({selection, saved});
