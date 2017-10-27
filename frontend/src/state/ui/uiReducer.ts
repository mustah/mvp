import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {TabsState} from '../../usecases/common/components/tabs/models/TabsModel';
import {tabs} from './tabs/tabsReducer';
import {indicator, IndicatorState} from './indicator/indicatorReducer';
import {PaginationState} from '../../usecases/ui/pagination/paginationModels';
import {pagination} from '../../usecases/ui/pagination/paginationReducer';

export interface UiState {
  tabs: TabsState;
  indicator: IndicatorState;
  sideMenu: SideMenuState;
  pagination: PaginationState;
}

export const ui = combineReducers<UiState>({
  tabs,
  indicator,
  sideMenu,
  pagination,
});
