import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {routes} from '../../app/routes';
import {makeToken} from '../../services/authService';
import {makeRestClient} from '../../services/restClient';
import {EndPoints} from '../../state/domain-models/domainModels';
import {User} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {Authorized, Unauthorized} from './authModels';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_USER = 'LOGOUT_USER';

export const AUTH_SET_USER_INFO = ' AUTH_SET_USER_INFO';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = createPayloadAction<string, Authorized>(LOGIN_SUCCESS);
export const loginFailure = createPayloadAction<string, Unauthorized>(LOGIN_FAILURE);

export const logoutUser = createEmptyAction(LOGOUT_USER);

export const authSetUser = createPayloadAction<string, User>(AUTH_SET_USER_INFO);

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const token = makeToken(username, password);
      const {data: user} = await makeRestClient(token).get(EndPoints.authenticate);
      dispatch(loginSuccess({token, user}));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(data));
    }
  };
};

export const logout = (organisationId: uuid) => {
  return async (dispatch) => {
    dispatch(logoutUser());
    dispatch(routerActions.push(`${routes.login}/${organisationId}`));
  };
};
