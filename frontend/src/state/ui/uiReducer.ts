import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {MessageState} from './message/messageModels';
import {message} from './message/messageReducer';
import {PaginationState} from './pagination/paginationModels';
import {pagination} from './pagination/paginationReducer';
import {TabsState} from './tabs/tabsModels';
import {tabs} from './tabs/tabsReducer';
import {ToolbarState} from './toolbar/toolbarModels';
import {toolbar} from './toolbar/toolbarReducer';

export interface UiState {
  message: MessageState;
  pagination: PaginationState;
  sideMenu: SideMenuState;
  tabs: TabsState;
  toolbar: ToolbarState;
}

export const ui = combineReducers<UiState>({
  message,
  pagination,
  sideMenu,
  tabs,
  toolbar,
});
