import {combineReducers} from 'redux';
import {SelectionState} from './selection/selectionModels';
import {saved, selection} from './selection/selectionReducer';

export interface SearchParameterState {
  selection: SelectionState;
  saved: SelectionState[];
}

export const searchParameters = combineReducers<SearchParameterState>({selection, saved});
