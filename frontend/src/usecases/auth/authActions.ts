import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {createRestClient} from '../../services/restClient';
import {tokenService, TokenService} from '../../services/TokenService';
import {routes} from '../app/routes';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_REQUEST = 'LOGOUT_REQUEST';
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = createPayloadAction(LOGIN_SUCCESS);
export const loginFailure = createPayloadAction(LOGIN_FAILURE);

export const logoutRequest = createEmptyAction(LOGOUT_REQUEST);
export const logoutSuccess = createEmptyAction(LOGOUT_SUCCESS);

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const token = TokenService.makeToken(username, password);
      const {data: user} = await createRestClient(token).get('/users/1');
      dispatch(loginSuccess({token, user}));
      tokenService.setToken(token);
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(data));
    }
  };
};

export const logout = () => {
  return async (dispatch) => {
    dispatch(logoutRequest());
    dispatch(logoutSuccess());
    tokenService.clear();
    dispatch(routerActions.push(routes.home));
  };
};
