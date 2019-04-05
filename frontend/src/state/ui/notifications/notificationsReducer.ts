import {ActionType, getType} from 'typesafe-actions';
import {logoutUser} from '../../../usecases/auth/authActions';
import * as actions from './notificationsActions';
import {NotificationsState} from './notificationsModels';

export const initialState: NotificationsState = {
  hasNotifications: false,
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser>;

export const notifications = (state: NotificationsState = initialState, action: ActionTypes): NotificationsState => {
  switch (action.type) {
    case getType(actions.getCurrentVersion):
      return {
        ...state,
        hasNotifications: state.hasNotifications || state.version !== action.payload,
        version: action.payload
      };
    case getType(actions.seenNotifications):
      return {...state, hasNotifications: false, version: action.payload};
    default:
      return state;
  }
};
