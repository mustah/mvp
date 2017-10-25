import {combineReducers} from 'redux';
import {filter} from './filter/filterReducer';
import {SearchParameterState} from './selection/selectionModels';
import {selection} from './selection/selectionReducer';

export const searchParameters = combineReducers<SearchParameterState>({selection, filter});
