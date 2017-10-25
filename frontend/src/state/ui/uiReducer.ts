import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {TabsState} from '../../usecases/common/components/tabs/models/TabsModel';
import {tabs} from './tabsReducer';
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
