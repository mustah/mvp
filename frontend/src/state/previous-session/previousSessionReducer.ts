import {isActionOf} from 'typesafe-actions';
import {Action, uuid} from '../../types/Types';
import {loginSuccess} from '../../usecases/auth/authActions';
import {Authorized} from '../../usecases/auth/authModels';

export interface PreviousSessionState {
  lastLoggedInUserId?: uuid;
}

const initialPreviousState: PreviousSessionState = {};

type ActionTypes = Action<Authorized>;

export const previousSession = (state: PreviousSessionState = initialPreviousState, action: ActionTypes) => {
  if (isActionOf(loginSuccess, action)) {
    return {
      ...state,
      lastLoggedInUserId: (action.payload as Authorized).user.id,
    };
  }
  return state;
};
