import {routerActions} from 'react-router-redux';
import {createAction, createStandardAction} from 'typesafe-actions';
import {routes} from '../../app/routes';
import {config} from '../../config/config';
import {translatedErrorMessage} from '../../helpers/translations';
import {makeThemeUrlOf} from '../../helpers/urlFactory';
import {GetState} from '../../reducers/rootReducer';
import {makeToken} from '../../services/authService';
import {EndPoints} from '../../services/endPoints';
import {authenticate, restClient, restClientWith} from '../../services/restClient';
import {User} from '../../state/domain-models/user/userModels';
import {changeLanguage} from '../../state/language/languageActions';
import {getCurrentVersion} from '../../state/ui/notifications/notificationsActions';
import {uuid} from '../../types/Types';
import {fetchTheme} from '../theme/themeActions';
import {Authorized, AuthState, Unauthorized} from './authModels';
import {getOrganisationSlug} from './authSelectors';

export const loginRequest = createAction('LOGIN_REQUEST');
export const loginSuccess = createStandardAction('LOGIN_SUCCESS')<Authorized>();
export const loginFailure = createStandardAction('LOGIN_FAILURE')<Unauthorized>();

export const logoutUser = createStandardAction('LOGOUT_USER')<Unauthorized | undefined>();

export const authSetUser = createStandardAction('AUTH_SET_USER_INFO')<User>();

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
      dispatch(getCurrentVersion(config().frontendVersion));
      await dispatch(fetchTheme(makeThemeUrlOf(user.organisation.slug)));
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
  async (dispatch, getState: GetState): Promise<void> => {
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
