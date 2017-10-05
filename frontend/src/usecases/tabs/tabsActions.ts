import {createPayloadAction} from 'react-redux-typescript';

export const TABS_CHANGE_TAB = 'TABS_CHANGE_TAB';
export const TABS_CHANGE_TAB_OPTION = 'TABS_CHANGE_TAB_OPTION';

export const changeTab = createPayloadAction(TABS_CHANGE_TAB);
export const changeTabOption = createPayloadAction(TABS_CHANGE_TAB_OPTION);
