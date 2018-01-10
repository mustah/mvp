import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';

export const SHOW_MESSAGE = 'SHOW_MESSAGE';
const createMessage = createPayloadAction<string, string>(SHOW_MESSAGE);

export const HIDE_MESSAGE = 'HIDE_MESSAGE';
const destroyMessage = createEmptyAction<string>(HIDE_MESSAGE);

export const showMessage = (message: string) =>
  (dispatch) => dispatch(createMessage(message));

export const hideMessage = () =>
  (dispatch) => dispatch(destroyMessage());
