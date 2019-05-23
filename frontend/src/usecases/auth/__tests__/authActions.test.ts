import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'connected-react-router';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeUser} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {config} from '../../../config/config';
import {makeThemeUrlOf} from '../../../helpers/urlFactory';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../services/endPoints';
import {authenticate} from '../../../services/restClient';
import {DomainModelsState} from '../../../state/domain-models/domainModels';
import {initialDomain} from '../../../state/domain-models/domainModelsReducer';
import {User} from '../../../state/domain-models/user/userModels';
import {changeLanguageRequest} from '../../../state/language/languageActions';
import {LanguageState} from '../../../state/language/languageModels';
import {getCurrentVersion} from '../../../state/ui/notifications/notificationsActions';
import {makeBody, requestTheme, successTheme} from '../../theme/themeActions';
import {Colors} from '../../theme/themeModels';
import {initialState as initialThemeState} from '../../theme/themeReducer';
import {authSetUser, login, loginFailure, loginRequest, loginSuccess, logout, logoutUser} from '../authActions';
import {Unauthorized} from '../authModels';

const configureMockStore = configureStore([thunk]);

describe('authActions', () => {
  const windowReload = window.location.reload;
  const version = config().frontendVersion;

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      users: {...initialDomain()},
    };
    const initialLanguageState: LanguageState = {language: {code: 'en'}};

    store = configureMockStore({
      domainModels: initialState,
      language: initialLanguageState,
      previousSession: {},
      theme: initialThemeState,
    });
    mockRestClient = new MockAdapter(axios);
    window.location.reload = () => void (0);
    authenticate('test');
    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });
  });

  afterEach(() => {
    mockRestClient.reset();
    window.location.reload = windowReload;
  });

  const user: User = makeUser();

  const themeBody = makeBody('red', 'green');
  const color: Colors = {primary: 'red', secondary: 'green'};

  let mockRestClient;
  let store;

  describe('authorized users', () => {

    const token = '123-123-123';

    const dispatchLogin = async (user: User) => {
      const username = 'the.batman@dc.com';
      const password = 'test1234';

      mockRestClient.onGet(EndPoints.authenticate).reply(200, {user, token});
      mockRestClient.onGet(makeThemeUrlOf(user.organisation.slug)).reply(200, themeBody);
      mockRestClient.onGet(EndPoints.logout).reply(204);

      return store.dispatch(login(username, password));
    };

    it('dispatches the login action', async () => {
      await dispatchLogin(user);

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token, user}),
        getCurrentVersion(version),
        requestTheme(),
        successTheme(color)
      ]);
    });

    it('logs in and changes language when user language differs from current language', async () => {
      const user: User = {...makeUser(), language: 'sv'};

      await dispatchLogin(user);

      expect(store.getActions()).toEqual([
        loginRequest(),
        changeLanguageRequest('sv'),
        loginSuccess({token, user}),
        getCurrentVersion(version),
        requestTheme(),
        successTheme(color)
      ]);
    });

    it('logs out logged in user', async () => {
      store = configureMockStore({auth: {user, isAuthenticated: true}});

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutUser(undefined),
        routerActions.push(`${routes.login}/${user.organisation.slug}`),
      ]);
    });

    it('does not logout unauthorized user', async () => {
      store = configureMockStore({auth: {user, isAuthenticated: false}});

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('un-authorized users', () => {

    it('cannot login when wrong credentials are provided ', async () => {
      const unauthorized = 401;
      const errorMessage: Unauthorized = {
        status: unauthorized,
        timestamp: Date.now(),
        error: 'Unauthorized',
        message: 'User is not authorized',
        path: '/api/v1/authenticate',
      };
      const username = 'foo';
      mockRestClient.onGet(EndPoints.authenticate).reply(unauthorized, errorMessage);

      await store.dispatch(login(username, '123123'));

      expect(store.getActions()).toEqual([loginRequest(), loginFailure(errorMessage)]);
    });

    it('cannot login when the server fails due to some unknown reason', async () => {
      const internalServerError = 500;
      const errorMessage: Unauthorized = {
        timestamp: Date.now(),
        status: internalServerError,
        error: 'Internal Server Error',
        message: 'Something when really wrong',
        path: '/api/v1/authenticate',
      };

      const username = 'foo';
      mockRestClient.onGet(EndPoints.authenticate).reply(internalServerError, errorMessage);

      await store.dispatch(login(username, '123123'));

      expect(store.getActions()).toEqual([loginRequest(), loginFailure(errorMessage)]);
    });

    it('does not persist the token if the user cannot login', async () => {
      const internalServerError = 401;
      const errorMessage: Unauthorized = {
        timestamp: Date.now(),
        status: internalServerError,
        error: 'Bad credentials',
        message: 'You are not allowed here',
        path: '/api/v1/authenticate',
      };

      const username = 'foo';
      mockRestClient.onGet(EndPoints.authenticate).reply(internalServerError, errorMessage);

      await store.dispatch(login(username, '123123'));
    });
  });

  describe('Unauthorized user', () => {

    it('can logout user without any side effect', async () => {
      mockRestClient.onGet(EndPoints.logout).reply(204);
      store = configureMockStore({auth: {user, isAuthenticated: true}});

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutUser(undefined),
        routerActions.push(`${routes.login}/${user.organisation.slug}`),
      ]);
    });
  });

  describe('set user info', () => {
    const newName = 'eva nilsson';
    const modifiedUser = {...user, name: newName};

    it('sets the user info to the provided user', async () => {
      await store.dispatch(authSetUser(modifiedUser));

      expect(store.getActions()).toEqual([authSetUser(modifiedUser)]);
    });
  });
});
