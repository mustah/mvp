import {TOGGLE_SHOW_HIDE_SIDE_MENU} from './sideMenuActions';
import {EmptyAction} from 'ts-redux-actions';

export interface SideMenuState {
  isOpen: boolean;
}

type ActionTypes = EmptyAction<string>;

export const sideMenu = (state: SideMenuState = {isOpen: true}, action: ActionTypes): SideMenuState => {
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
