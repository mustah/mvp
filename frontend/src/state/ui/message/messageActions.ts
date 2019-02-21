import {createAction, createStandardAction} from 'typesafe-actions';

export const showSuccessMessage = createStandardAction('SHOW_SUCCESS_MESSAGE')<string>();
export const showFailMessage = createStandardAction('SHOW_FAIL_MESSAGE')<string>();
export const hideMessage = createAction('HIDE_MESSAGE');
