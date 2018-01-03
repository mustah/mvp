import axios from 'axios';
import * as MockAdapter from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {makeToken} from '../../../services/authService';
import {login, loginFailure, loginRequest, loginSuccess, logout, logoutRequest, logoutSuccess} from '../authActions';
import {Unauthorized, User} from '../authModels';

const middlewares = [thunk];
const mockStore = configureStore(middlewares);

describe('authActions', () => {

  const user: User = {
    id: 1,
    name: 'clark',
    email: 'ck@dailyplanet.net',
    company: {id: 'daily planet', name: 'daily planet', code: 'daily-planet'},
    roles: [Role.USER],
  };
  let mockRestClient;
  let store;
  let token: string;

  beforeEach(() => {
    store = mockStore({});
    mockRestClient = new MockAdapter(axios);
  });

  describe('authorized users', () => {

    const dispatchLogin = async () => {

      const username = 'the.batman@dc.com';
      const password = 'test1234';

      mockRestClient.onGet(`/authenticate/${username}`).reply(200, user);

      token = makeToken(username, password);

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
      await dispatchLogin();

      await store.dispatch(logout(user.company.code));

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token, user}),
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(`${routes.login}/${user.company.code}`),
      ]);
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
        path: '/api/authenticate',
      };
      const username = 'foo';
      mockRestClient.onGet(`/authenticate/${username}`).reply(unauthorized, errorMessage);

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
        path: '/api/authenticate',
      };

      const username = 'foo';
      mockRestClient.onGet(`/authenticate/${username}`).reply(internalServerError, errorMessage);

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
        path: '/api/authenticate',
      };

      const username = 'foo';
      mockRestClient.onGet(`/authenticate/${username}`).reply(internalServerError, errorMessage);

      await store.dispatch(login(username, '123123'));
    });

    it('logs out user without any side effect', async () => {
      await store.dispatch(logout(user.company.code));

      expect(store.getActions()).toEqual([
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(`${routes.login}/${user.company.code}`),
      ]);
    });
  });

});
