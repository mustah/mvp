import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';

export const SHOW_SUCCESS_MESSAGE = 'SHOW_SUCCESS_MESSAGE';
export const SHOW_FAIL_MESSAGE = 'SHOW_FAIL_MESSAGE';
export const HIDE_MESSAGE = 'HIDE_MESSAGE';

export const showSuccessMessage = createPayloadAction<string, string>(SHOW_SUCCESS_MESSAGE);
export const showFailMessage = createPayloadAction<string, string>(SHOW_FAIL_MESSAGE);
export const hideMessage = createEmptyAction<string>(HIDE_MESSAGE);
