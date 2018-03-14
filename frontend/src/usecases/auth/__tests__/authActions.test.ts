import axios from 'axios';
import * as MockAdapter from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {EndPoints} from '../../../services/endPoints';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {
  AUTH_SET_USER_INFO,
  authSetUser,
  login,
  loginFailure,
  loginRequest,
  loginSuccess,
  logout,
  logoutUser,
} from '../authActions';
import {Unauthorized} from '../authModels';

const middlewares = [thunk];
const mockStore = configureStore(middlewares);

describe('authActions', () => {

  const user: User = {
    id: 1,
    name: 'clark',
    email: 'ck@dailyplanet.net',
    organisation: {id: 'daily planet', name: 'daily planet', code: 'daily-planet'},
    roles: [Role.USER],
  };
  let mockRestClient;
  let store;

  beforeEach(() => {
    store = mockStore({});
    mockRestClient = new MockAdapter(axios);
  });

  describe('authorized users', () => {

    const token = '123-123-123';

    const dispatchLogin = async () => {
      const username = 'the.batman@dc.com';
      const password = 'test1234';

      mockRestClient.onGet(EndPoints.authenticate).reply(200, {user, token});
      mockRestClient.onGet(EndPoints.logout).reply(204);

      return store.dispatch(login(username, password));
    };

    it('dispatches the login action', async () => {
      await dispatchLogin();

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token, user}),
      ]);
    });

    it('logs out logged in user', async () => {
      store = mockStore({auth: {user, isAuthenticated: true}});

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutUser(undefined),
        routerActions.push(`${routes.login}/${user.organisation.code}`),
      ]);
    });

    it('does not logout unauthorized user', async () => {
      store = mockStore({auth: {user, isAuthenticated: false}});

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
        path: '/v1/api/authenticate',
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
        path: '/v1/api/authenticate',
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
        path: '/v1/api/authenticate',
      };

      const username = 'foo';
      mockRestClient.onGet(EndPoints.authenticate).reply(internalServerError, errorMessage);

      await store.dispatch(login(username, '123123'));
    });
  });

  describe('Unauthorized user', () => {

    it('can logout user without any side effect', async () => {
      mockRestClient.onGet(EndPoints.logout).reply(204);
      store = mockStore({auth: {user, isAuthenticated: true}});

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutUser(undefined),
        routerActions.push(`${routes.login}/${user.organisation.code}`),
      ]);
    });
  });

  describe('set user info', () => {
    const newName = 'eva nilsson';
    const modifiedUser = {...user, name: newName};

    it('sets the user info to the provided user', async () => {
      await store.dispatch(authSetUser(modifiedUser));

      expect(store.getActions()).toEqual([
        {type: AUTH_SET_USER_INFO, payload: modifiedUser},
      ]);
    });
  });
});
