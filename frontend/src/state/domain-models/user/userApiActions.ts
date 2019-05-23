import {routerActions} from 'connected-react-router';
import {routes} from '../../../app/routes';
import {EndPoints} from '../../../services/endPoints';
import {restClientWith} from '../../../services/restClient';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dispatch, ErrorResponse, uuid} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {Authorized} from '../../../usecases/auth/authModels';
import {changeLanguageWithRefresh} from '../../language/languageActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {
  clearError,
  deleteRequest,
  fetchEntityIfNeeded,
  fetchIfNeeded,
  postRequest,
  putRequest,
  putRequestToUrl,
} from '../domainModelsActions';
import {Password, User} from './userModels';
import {usersDataFormatter} from './userSchema';

export const fetchUsers = fetchIfNeeded<User>(
  EndPoints.users,
  'users',
  usersDataFormatter,
);

export const fetchUser = fetchEntityIfNeeded<User>(EndPoints.users, 'users');

export const addUser = postRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch) => {
    dispatch(showSuccessMessage(firstUpperTranslated(
      'successfully created the user {{name}} ({{email}})',
      {...user},
    )));
    dispatch(routerActions.push(`${routes.adminUsers}`));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create user: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const modifyUser = putRequest<User, User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch) => {
    dispatch(showSuccessMessage(firstUpperTranslated(
      'successfully updated user {{name}} ({{email}})',
      {...user},
    )));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update user: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const modifyProfile = putRequest<User, User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch) => {
    dispatch(authSetUser(user));
    dispatch(changeLanguageWithRefresh(user.language));
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated profile', {...user})));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update profile: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const changePassword = putRequestToUrl<Authorized, Password, uuid>(EndPoints.changePassword, {
    afterSuccess: (authorized: Authorized, dispatch: Dispatch) => {
      const {user, token} = authorized;

      restClientWith(token);
      dispatch(authSetUser(user));
      dispatch(changeLanguageWithRefresh(user.language));
      dispatch(showSuccessMessage(firstUpperTranslated('successfully updated password')));
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
      dispatch(showFailMessage(firstUpperTranslated(
        'failed to update profile: {{error}}',
        {error: firstUpperTranslated(message.toLowerCase())},
      )));
    },
  },
  (userId: uuid) => `${EndPoints.changePassword}/${userId}`
);

export const deleteUser = deleteRequest<User>(EndPoints.users, {
    afterSuccess: (user: User, dispatch: Dispatch) => {
      dispatch(
        showSuccessMessage(firstUpperTranslated(
          'successfully deleted the user {{name}} ({{email}})',
          {...user},
        )),
      );
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
      dispatch(showFailMessage(firstUpperTranslated(
        'failed to delete the user: {{error}}',
        {error: firstUpperTranslated(message.toLowerCase())},
      )));
    },
  },
);

export const clearUserError = clearError(EndPoints.users);
