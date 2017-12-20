import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {routes} from '../../app/routes';
import {makeToken} from '../../services/authService';
import {makeRestClient} from '../../services/restClient';
import {Authorized, Unauthorized} from './authModels';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_REQUEST = 'LOGOUT_REQUEST';
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = createPayloadAction<string, Authorized>(LOGIN_SUCCESS);
export const loginFailure = createPayloadAction<string, Unauthorized>(LOGIN_FAILURE);

export const logoutRequest = createEmptyAction(LOGOUT_REQUEST);
export const logoutSuccess = createEmptyAction(LOGOUT_SUCCESS);

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const token = makeToken(username, password);
      const {data: user} = await makeRestClient(token).get('/authenticate/' + username);
      dispatch(loginSuccess({token, user}));
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
    dispatch(routerActions.push(routes.home));
  };
};
