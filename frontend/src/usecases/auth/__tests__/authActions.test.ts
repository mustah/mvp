import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {FakeStorage} from '../../../services/__tests__/FakeStorage';
import {initialAuthState, loadAuthState} from '../../../services/authService';
import {storageService, StorageService} from '../../../services/StorageService';
import {routes} from '../../app/routes';
import {login, loginFailure, loginRequest, loginSuccess, logout, logoutRequest, logoutSuccess} from '../authActions';
import {UnauthorizedDTO} from '../authReducer';

const middlewares = [thunk];
const mockStore = configureStore(middlewares);

describe('authActions', () => {

  const user = {id: 1, firstName: 'clark', lastName: 'kent'};
  let mockRestClient;
  let store;

  beforeEach(() => {
    store = mockStore({});
    mockRestClient = new MockAdapter(axios);

    const fakeTokenService = new StorageService(new FakeStorage());

    storageService.getItem = jest.fn((key) => {
      return fakeTokenService.getItem(key);
    });

    storageService.setItem = jest.fn((key: string, data: string) => {
      return fakeTokenService.setItem(key, data);
    });

    storageService.removeItem = jest.fn((key: string) => {
      return fakeTokenService.removeItem(key);
    });
  });

  describe('authorized users', () => {

    const dispatchLogin = async () => {
      mockRestClient.onGet('/users/1').reply(200, user);

      return store.dispatch(login('the.batman@dc.com', 'test1234'));
    };

    it('dispatches the login action', async () => {
      await dispatchLogin();

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token: loadAuthState()!.token, user}),
      ]);
    });

    it('logs out logged in user', async () => {
      await dispatchLogin();

      const tokenBeforeLogout = loadAuthState()!.token;

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token: tokenBeforeLogout, user}),
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);

      expect(loadAuthState()).toEqual(initialAuthState);
    });

    it('saves auth state to local storage', async () => {
      await dispatchLogin();

      expect(loadAuthState()).toEqual({
        isAuthenticated: true,
        token: 'dGhlLmJhdG1hbkBkYy5jb206dGVzdDEyMzQ=',
        user: {firstName: 'clark', id: 1, lastName: 'kent'},
      });
    });
  });

  describe('un-authorized users', () => {

    it('cannot login when wrong credentials are provided ', async () => {
      const unauthorized = 401;
      const errorMessage: UnauthorizedDTO = {
        status: unauthorized,
        timestamp: Date.now(),
        error: 'Unauthorized',
        message: 'User is not authorized',
        path: '/api/users/1',
      };
      mockRestClient.onGet('/users/1').reply(unauthorized, errorMessage);

      await store.dispatch(login('foo', '123123'));

      expect(store.getActions()).toEqual([loginRequest(), loginFailure(errorMessage)]);
      expect(loadAuthState()).toEqual(initialAuthState);
    });

    it('cannot login when the server fails due to some unknown reason', async () => {
      const internalServerError = 500;
      const errorMessage: UnauthorizedDTO = {
        timestamp: Date.now(),
        status: internalServerError,
        error: 'Internal Server Error',
        message: 'Something when really wrong',
        path: '/api/users/1',
      };
      mockRestClient.onGet('/users/1').reply(internalServerError, errorMessage);

      await store.dispatch(login('foo', '123123'));

      expect(store.getActions()).toEqual([loginRequest(), loginFailure(errorMessage)]);
      expect(loadAuthState()).toEqual(initialAuthState);
    });

    it('does not persist the token if the user cannot login', async () => {
      const internalServerError = 401;
      const errorMessage: UnauthorizedDTO = {
        timestamp: Date.now(),
        status: internalServerError,
        error: 'Bad credentials',
        message: 'You are not allowed here',
        path: '/api/users/1',
      };
      mockRestClient.onGet('/users/1').reply(internalServerError, errorMessage);

      await store.dispatch(login('foo', '123123'));

      expect(loadAuthState()).toEqual(initialAuthState);
    });

    it('logs out user without any side effect', async () => {
      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);

      expect(loadAuthState()).toEqual(initialAuthState);
    });
  });

});
