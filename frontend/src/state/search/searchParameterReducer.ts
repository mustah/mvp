import {combineReducers} from 'redux';
import {SearchParameterState} from './selection/selectionModels';
import {selection} from './selection/selectionReducer';

export const searchParameters = combineReducers<SearchParameterState>({selection});
