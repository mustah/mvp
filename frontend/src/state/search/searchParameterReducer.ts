import {combineReducers} from 'redux';
import {selection} from 'state/search/selection/selectionReducer';

export const searchParameters = combineReducers<SearchParameterState>(selection, filter);
