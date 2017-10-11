import {createEmptyAction} from 'react-redux-typescript';

export const TOGGLE_SHOW_HIDE_SIDE_MENU = 'TOGGLE_SHOW_HIDE_SIDE_MENU';

export const toggleShowHideSideMenu = createEmptyAction<string>(TOGGLE_SHOW_HIDE_SIDE_MENU);
