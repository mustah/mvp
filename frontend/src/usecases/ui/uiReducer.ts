import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../sidemenu/sideMenuReducer';
import {TabsState} from '../tabs/models/TabsModel';
import {tabs} from '../tabs/tabsReducer';
import {indicator, IndicatorState} from './indicatorReducer';

export interface UiState {
  tabs: TabsState;
  indicator: IndicatorState;
  sideMenu: SideMenuState;
}

export const ui = combineReducers<UiState>({
  tabs,
  indicator,
  sideMenu,
});
