import {getType} from 'typesafe-actions';
import {Action} from '../../../types/Types';
import {hideMessage, showFailMessage, showSuccessMessage} from './messageActions';
import {MessageState} from './messageModels';

const initialState: MessageState = {
  isOpen: false,
  message: '',
};

export const message = (state: MessageState = initialState, action: Action<string>): MessageState => {
  switch (action.type) {
    case getType(showSuccessMessage):
      return {
        isOpen: true,
        message: action.payload,
        messageType: 'success',
      };
    case getType(showFailMessage):
      return {
        isOpen: true,
        message: action.payload,
        messageType: 'fail',
      };
    case getType(hideMessage):
      return {
        ...state,
        isOpen: false,
        message: '',
      };
    default:
      return state;
  }
};
