import {combineReducers} from 'redux';
import {selection} from './selection/selectionReducer';
import {SelectionState} from './selection/selectionModels';

export interface SearchParameterState {
  selection: SelectionState;
}

export const searchParameters = combineReducers<SearchParameterState>({selection});
