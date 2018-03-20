import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {routes} from '../../app/routes';
import {GetState} from '../../reducers/rootReducer';
import {makeToken} from '../../services/authService';
import {authenticate, restClient, restClientWith} from '../../services/restClient';
import {EndPoints} from '../../services/endPoints';
import {User} from '../../state/domain-models/user/userModels';
import {Authorized, AuthState, Unauthorized} from './authModels';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_USER = 'LOGOUT_USER';

export const AUTH_SET_USER_INFO = ' AUTH_SET_USER_INFO';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = createPayloadAction<string, Authorized>(LOGIN_SUCCESS);
export const loginFailure = createPayloadAction<string, Unauthorized>(LOGIN_FAILURE);

export const logoutUser = createPayloadAction<string, Unauthorized | undefined>(LOGOUT_USER);

export const authSetUser = createPayloadAction<string, User>(AUTH_SET_USER_INFO);

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const basicToken = makeToken(username, password);
      const {data: {user, token}} = await authenticate(basicToken).get(EndPoints.authenticate);
      restClientWith(token);
      dispatch(loginSuccess({token, user}));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(data));
    }
  };
};

export const isAuthenticated = (auth: AuthState): boolean => !!auth.user && auth.isAuthenticated;

export const logout = (error?: Unauthorized) => {
  return async (dispatch, getState: GetState) => {
    const {auth} = getState();
    if (isAuthenticated(auth)) {
      try {
        dispatch(logoutUser(error));
        dispatch(routerActions.push(`${routes.login}/${auth.user!.organisation.slug}`));
        await restClient.get(EndPoints.logout);
      } catch (ignore) {
        // tslint:disable
      }
    }
  }
};
