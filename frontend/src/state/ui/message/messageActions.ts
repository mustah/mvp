import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';

export const SHOW_MESSAGE = 'SHOW_MESSAGE';
export const HIDE_MESSAGE = 'HIDE_MESSAGE';

export const showMessage = createPayloadAction<string, string>(SHOW_MESSAGE);
export const hideMessage = createEmptyAction<string>(HIDE_MESSAGE);
