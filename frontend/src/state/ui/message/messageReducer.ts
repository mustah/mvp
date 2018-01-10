import {Action} from '../../../types/Types';
import {HIDE_MESSAGE, SHOW_MESSAGE} from './messageActions';
import {MessageState} from './messageModels';

const initialState: MessageState = {
  isOpen: false,
  message: '',
};

export const message = (state: MessageState = initialState, action: Action<any>): MessageState => {
  switch (action.type) {
    case SHOW_MESSAGE:
      return {
        isOpen: true,
        message: action.payload,
      };
    case HIDE_MESSAGE:
      return {...initialState};
  }
  return state;
};
