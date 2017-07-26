import {AnyAction} from 'redux';
import {VALIDATION_REQUEST} from '../../types/ActionTypes';
import {ValidationState} from './models/Validation';

const initialState: ValidationState = {
  title: 'ValidationState',
  records: [],
  isFetching: false,
};

export const validation = (state: ValidationState = initialState, action: AnyAction): ValidationState => {
  switch (action.type) {
    case VALIDATION_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    default:
      return state;
  }
};
