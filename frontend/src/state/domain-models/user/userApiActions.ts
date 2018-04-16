import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {changeLanguage} from '../../language/languageActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {EndPoints} from '../../../services/endPoints';
import {
  clearError,
  deleteRequest,
  fetchEntityIfNeeded,
  fetchIfNeeded,
  postRequest,
  putRequest,
} from '../domainModelsActions';
import {User} from './userModels';
import {userSchema} from './userSchema';

export const fetchUsers = fetchIfNeeded<User>(EndPoints.users, userSchema, 'users');

export const fetchUser = fetchEntityIfNeeded<User>(EndPoints.users, 'users');

export const addUser = postRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated(
      'successfully created the user {{name}} ({{email}})',
      {...user},
    )));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create user: {{error}}',
      {error: error.message},
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
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update user: {{error}}',
      {error: error.message},
    )));
  },
});

export const modifyProfile = putRequest<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(authSetUser(user));
    dispatch(changeLanguage(user.language));
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated profile', {...user})));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update profile: {{error}}',
      {error: error.message},
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
    afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
      dispatch(showFailMessage(firstUpperTranslated(
        'failed to delete the user: {{error}}',
        {error: error.message},
      )));
    },
  },
);

export const clearUserError = clearError(EndPoints.users);
