import {emptyActionOf, payloadActionOf} from '../../../types/Types';

export const SHOW_SUCCESS_MESSAGE = 'SHOW_SUCCESS_MESSAGE';
export const SHOW_FAIL_MESSAGE = 'SHOW_FAIL_MESSAGE';
export const HIDE_MESSAGE = 'HIDE_MESSAGE';

export const showSuccessMessage = payloadActionOf<string>(SHOW_SUCCESS_MESSAGE);
export const showFailMessage = payloadActionOf<string>(SHOW_FAIL_MESSAGE);
export const hideMessage = emptyActionOf(HIDE_MESSAGE);
