import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {changeLanguage} from '../../language/languageActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {
  clearError,
  deleteRequest,
  fetchEntityIfNeeded,
  fetchIfNeeded,
  postRequest,
  putRequest,
} from '../domainModelsActions';
import {User} from './userModels';
import {usersDataFormatter} from './userSchema';

export const fetchUsers = fetchIfNeeded<User>(
  EndPoints.users,
  'users',
  usersDataFormatter,
);

export const fetchUser = fetchEntityIfNeeded<User>(EndPoints.users, 'users');

export const addUser = postRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated(
      'successfully created the user {{name}} ({{email}})',
      {...user},
    )));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create user: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const modifyUser = putRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated(
      'successfully updated user {{name}} ({{email}})',
      {...user},
    )));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update user: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const modifyProfile = putRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(authSetUser(user));
    dispatch(changeLanguage(user.language));
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated profile', {...user})));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update profile: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const deleteUser = deleteRequest<User>(EndPoints.users, {
    afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
      dispatch(
        showSuccessMessage(firstUpperTranslated(
          'successfully deleted the user {{name}} ({{email}})',
          {...user},
        )),
      );
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
      dispatch(showFailMessage(firstUpperTranslated(
        'failed to delete the user: {{error}}',
        {error: firstUpperTranslated(message.toLowerCase())},
      )));
    },
  },
);

export const clearUserError = clearError(EndPoints.users);
