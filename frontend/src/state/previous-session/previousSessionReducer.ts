import {Action, uuid} from '../../types/Types';
import {LOGIN_SUCCESS} from '../../usecases/auth/authActions';
import {Authorized} from '../../usecases/auth/authModels';

export interface PreviousSessionState {
  lastLoggedInUserId?: uuid;
}

const initialPreviousState: PreviousSessionState = {};

type ActionTypes = Action<Authorized>;

export const previousSession = (state: PreviousSessionState = initialPreviousState, action: ActionTypes) => {
  if (action.type === LOGIN_SUCCESS) {
    return {
      ...state,
      lastLoggedInUserId: (action.payload as Authorized).user.id,
    };
  }
  return state;
};
