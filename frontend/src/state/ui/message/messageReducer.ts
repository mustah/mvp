import {Action} from '../../../types/Types';
import {HIDE_MESSAGE, SHOW_FAIL_MESSAGE, SHOW_SUCCESS_MESSAGE} from './messageActions';
import {MessageState} from './messageModels';

const initialState: MessageState = {
  isOpen: false,
  message: '',
};

export const message = (state: MessageState = initialState, action: Action<any>): MessageState => {
  switch (action.type) {
    case SHOW_SUCCESS_MESSAGE:
      return {
        isOpen: true,
        message: action.payload,
        messageType: 'success',
      };
      case SHOW_FAIL_MESSAGE:
      return {
        isOpen: true,
        message: action.payload,
        messageType: 'fail',
      };
    case HIDE_MESSAGE:
      return {
        ...state,
        isOpen: false,
        message: '',
      };
    default:
      return state;
  }
};
