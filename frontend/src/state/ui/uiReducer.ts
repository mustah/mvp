import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {indicator, IndicatorState} from './indicator/indicatorReducer';
import {MessageState} from './message/messageModels';
import {message} from './message/messageReducer';
import {PaginationState} from './pagination/paginationModels';
import {pagination} from './pagination/paginationReducer';
import {SelectionTreeUiState} from './selection-tree/selectionTreeModels';
import {selectionTree} from './selection-tree/selectionTreeReducer';
import {TabsState} from './tabs/tabsModels';
import {tabs} from './tabs/tabsReducer';
import {ToolbarState} from './toolbar/toolbarModels';
import {toolbar} from './toolbar/toolbarReducer';

export interface UiState {
  indicator: IndicatorState;
  message: MessageState;
  pagination: PaginationState;
  selectionTree: SelectionTreeUiState;
  sideMenu: SideMenuState;
  tabs: TabsState;
  toolbar: ToolbarState;
}

export const ui = combineReducers<UiState>({
  indicator,
  message,
  pagination,
  selectionTree,
  sideMenu,
  tabs,
  toolbar,
});
