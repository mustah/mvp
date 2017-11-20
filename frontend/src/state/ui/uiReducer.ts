import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {TabsState} from '../../usecases/common/components/tabs/models/TabsModel';
import {tabs} from './tabs/tabsReducer';
import {indicator, IndicatorState} from './indicator/indicatorReducer';
import {PaginationState} from './pagination/paginationModels';
import {pagination} from './pagination/paginationReducer';
import {selectionTree} from './selection-tree/selectionTreeReducer';
import {SelectionTreeState} from './selection-tree/selectionTreeModels';

export interface UiState {
  tabs: TabsState;
  indicator: IndicatorState;
  sideMenu: SideMenuState;
  pagination: PaginationState;
  selectionTree: SelectionTreeState;
}

export const ui = combineReducers<UiState>({
  tabs,
  indicator,
  sideMenu,
  pagination,
  selectionTree,
});
