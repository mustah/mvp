import {AnyAction} from 'redux';
import {TOGGLE_SHOW_HIDE_SIDE_MENU} from './sideMenuActions';

export interface SideMenuState {
  isOpen: boolean;
}

export const sideMenu = (state: SideMenuState = {isOpen: true}, action: AnyAction): SideMenuState => {
  switch (action.type) {
    case TOGGLE_SHOW_HIDE_SIDE_MENU:
      return {
        ...state,
        isOpen: !state.isOpen,
      };
    default:
      return state;
  }
};
