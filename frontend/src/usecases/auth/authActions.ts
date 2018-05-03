import {createEmptyAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {routes} from '../../app/routes';
import {translatedErrorMessage} from '../../helpers/translations';
import {GetState} from '../../reducers/rootReducer';
import {makeToken} from '../../services/authService';
import {EndPoints} from '../../services/endPoints';
import {authenticate, restClient, restClientWith} from '../../services/restClient';
import {User} from '../../state/domain-models/user/userModels';
import {changeLanguage} from '../../state/language/languageActions';
import {payloadActionOf} from '../../types/Types';
import {Authorized, AuthState, Unauthorized} from './authModels';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_USER = 'LOGOUT_USER';

export const AUTH_SET_USER_INFO = ' AUTH_SET_USER_INFO';

export const loginRequest = createEmptyAction(LOGIN_REQUEST);
export const loginSuccess = payloadActionOf<Authorized>(LOGIN_SUCCESS);
export const loginFailure = payloadActionOf<Unauthorized>(LOGIN_FAILURE);

export const logoutUser = payloadActionOf<Unauthorized | undefined>(LOGOUT_USER);

export const authSetUser = payloadActionOf<User>(AUTH_SET_USER_INFO);

const translatedError = (error?: Unauthorized): Unauthorized | undefined =>
  error ? ({...error, message: translatedErrorMessage(error.message)}) : undefined;

const isAuthenticated = (auth: AuthState): boolean => !!auth.user && auth.isAuthenticated;

export const login = (username: string, password: string) => {
  return async (dispatch) => {
    dispatch(loginRequest());
    try {
      const basicToken = makeToken(username, password);
      const {data: {user, token}} = await authenticate(basicToken).get(EndPoints.authenticate);
      restClientWith(token);
      await dispatch(changeLanguage(user.language));
      dispatch(loginSuccess({token, user}));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(translatedError(data)!));
    }
  };
};

export const logout = (error?: Unauthorized) => {
  return async (dispatch, getState: GetState) => {
    const {auth} = getState();
    if (isAuthenticated(auth)) {
      try {
        dispatch(logoutUser(translatedError(error)));
        dispatch(routerActions.push(`${routes.login}/${auth.user!.organisation.slug}`));
        await restClient.get(EndPoints.logout);
      } catch (ignore) {
        // tslint:disable
      }
    }
  };
};
