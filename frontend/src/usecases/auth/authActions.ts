import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {createRestClient} from '../../services/restClient';
import {tokenService, TokenService} from '../../services/TokenService';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = createPayloadAction(LOGIN_SUCCESS);
export const loginFailure = createPayloadAction(LOGIN_FAILURE);

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const token = TokenService.makeToken(username, password);
      tokenService.setToken(token);

      const {data: user} = await createRestClient(token).get('/users/1');
      dispatch(loginSuccess({token, user}));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(data));
    }
  };
};
