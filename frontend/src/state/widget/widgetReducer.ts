import {combineReducers} from 'redux';
import {widgetData, WidgetDataState} from './data/widgetDataReducer';

export interface WidgetState {
  data: WidgetDataState;
}

export const widget = combineReducers({data: widgetData});
