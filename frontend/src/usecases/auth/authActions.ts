import {routerActions} from 'react-router-redux';
import {routes} from '../../app/routes';
import {translatedErrorMessage} from '../../helpers/translations';
import {GetState} from '../../reducers/rootReducer';
import {makeToken} from '../../services/authService';
import {EndPoints} from '../../services/endPoints';
import {authenticate, restClient, restClientWith} from '../../services/restClient';
import {User} from '../../state/domain-models/user/userModels';
import {changeLanguage} from '../../state/language/languageActions';
import {emptyActionOf, payloadActionOf, uuid} from '../../types/Types';
import {Authorized, AuthState, Unauthorized} from './authModels';
import {getOrganisationSlug} from './authSelectors';

export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_USER = 'LOGOUT_USER';

export const AUTH_SET_USER_INFO = ' AUTH_SET_USER_INFO';

export const loginRequest = emptyActionOf(LOGIN_REQUEST);
export const loginSuccess = payloadActionOf<Authorized>(LOGIN_SUCCESS);
export const loginFailure = payloadActionOf<Unauthorized>(LOGIN_FAILURE);

export const logoutUser = payloadActionOf<Unauthorized | undefined>(LOGOUT_USER);

export const authSetUser = payloadActionOf<User>(AUTH_SET_USER_INFO);

const translatedError = (error?: Unauthorized): Unauthorized | undefined =>
  error ? ({...error, message: translatedErrorMessage(error.message)}) : undefined;

const isAuthenticated = (auth: AuthState): boolean => !!auth.user && auth.isAuthenticated;

interface AuthApiResponse {
  data: {
    token: string;
    user: User;
  };
}

export const login = (username: string, password: string) =>
  async (dispatch, getState: GetState) => {
    dispatch(loginRequest());
    try {
      const basicToken = makeToken(username, password);
      const previousUserId: uuid | undefined = getState().previousSession.lastLoggedInUserId;
      const {data: {user, token}}: AuthApiResponse = await authenticate(basicToken).get(EndPoints.authenticate);
      restClientWith(token);
      await dispatch(changeLanguage(user.language));
      dispatch(loginSuccess({token, user}));
      if (previousUserId && previousUserId !== user.id) {
        // cannot dispatch resetSelection() here when running tests (running application in browser is fine).
        // reason: unknown, possibly related to circular imports in non-bundled modes, such as yarn test
        dispatch({type: 'RESET_SELECTION'});
      }
    } catch (error) {
      const {response: {data}} = error;
      dispatch(loginFailure(translatedError(data)!));
    }
  };

export const logout = (error?: Unauthorized) =>
  async (dispatch, getState: GetState) => {
    const {auth} = getState();
    if (isAuthenticated(auth)) {
      try {
        dispatch(logoutUser(translatedError(error)));
        dispatch(routerActions.push(`${routes.login}/${getOrganisationSlug(auth)}`));
        await restClient.get(EndPoints.logout);
      } catch (ignore) {
        // tslint:disable
      }
    }
  };
