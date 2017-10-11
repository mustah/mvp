import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {config} from '../../config/config';
import {makeToken} from '../../services/authService';
import {makeRestClient} from '../../services/restClient';
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

    if (config().useJsonServerInsteadOfJavaBackend) {
      // TODO remove this mocking layer
      const mockedUsers = {
        adam: {
          id: 7,
          firstName: 'Adam',
          lastName: 'Johnsson',
          email: 'adam@varme.se',
          company: 'Värme för alla AB',
        },
        default: {
          id: 7,
          firstName: 'Eva',
          lastName: 'Nilsson',
          email: 'evanil@elvaco.se',
          company: 'Bostäder AB',
        },
      };
      const user = mockedUsers.hasOwnProperty(username) ? mockedUsers[username] : mockedUsers.default;
      dispatch(loginSuccess({
        token: 'one two freddy is coming for you',
        user,
      }));
    } else {
      try {
        const token = makeToken(username, password);
        const {data: user} = await makeRestClient(token).get('/authenticate');
        dispatch(loginSuccess({token, user}));
      } catch (error) {
        const {response: {data}} = error;
        dispatch(loginFailure(data));
      }
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
