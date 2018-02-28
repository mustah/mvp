import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {EndPoints} from '../domainModels';
import {
  clearError,
  restDelete,
  restGetEntityIfNeeded,
  restGetIfNeeded,
  restPost,
  restPut,
} from '../domainModelsActions';
import {User} from './userModels';
import {userSchema} from './userSchema';

export const fetchUsers = restGetIfNeeded<User>(EndPoints.users, userSchema, 'users');
export const fetchUser = restGetEntityIfNeeded<User>(EndPoints.users, 'users');
export const addUser = restPost<User>(EndPoints.users, {
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
export const modifyUser = restPut<User>(EndPoints.users, {
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
export const modifyProfile = restPut<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated profile', {...user})));
    dispatch(authSetUser(user));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update profile: {{error}}',
      {error: error.message},
    )));
  },
});
export const deleteUser = restDelete<User>(EndPoints.users, {
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

export const clearErrorUsers = clearError(EndPoints.users);
