import {combineReducers} from 'redux';
import {TabsState} from '../tabs/models/TabsModel';
import {tabs} from '../tabs/tabsReducer';
import {indicator, IndicatorState} from './indicatorReducer';

export interface UiState {
  tabs: TabsState;
  indicator: IndicatorState;
}

export const ui = combineReducers<UiState>({
  tabs,
  indicator,
});
