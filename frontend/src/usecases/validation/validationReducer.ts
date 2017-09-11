import {AnyAction} from 'redux';
import {validation as initialState} from '../../store/initialAppState';
import {VALIDATION_REQUEST} from '../../types/ActionTypes';
import {ValidationState} from './models/Validations';

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
