import {combineReducers} from 'redux';
import {indicator, IndicatorState} from '../common/components/indicators/indicatorReducer';
import {TabsState} from '../tabs/models/TabsModel';
import {tabs} from '../tabs/tabsReducer';

export interface UiState {
  tabs: TabsState;
  indicator: IndicatorState;
}

export const ui = combineReducers<UiState>({
  tabs,
  indicator,
});
