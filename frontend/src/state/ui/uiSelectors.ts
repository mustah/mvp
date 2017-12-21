import {createSelector} from 'reselect';
import {SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {UiState} from './uiReducer';

const selectSideMenu = (state: UiState): SideMenuState => state.sideMenu;

export const isSideMenuOpen = createSelector<UiState, SideMenuState, boolean>(
  selectSideMenu,
  (sideMenu) => sideMenu.isOpen,
);
