import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {FakeStorage} from '../../../services/__tests__/FakeStorage';
import {tokenService, TokenService} from '../../../services/TokenService';
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

    const fakeTokenService = new TokenService(new FakeStorage());

    tokenService.getToken = jest.fn(() => {
      return fakeTokenService.getToken();
    });

    tokenService.setToken = jest.fn((token: string) => {
      return fakeTokenService.setToken(token);
    });

    tokenService.clear = jest.fn(() => {
      return fakeTokenService.clear();
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
        loginSuccess({token: tokenService.getToken(), user}),
      ]);
    });

    it('logs out logged in user', async () => {
      await dispatchLogin();

      const tokenBeforeLogout = tokenService.getToken();

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token: tokenBeforeLogout, user}),
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);

      expect(tokenService.getToken()).toBeUndefined();
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

      expect(tokenService.getToken()).toBeUndefined();
    });

    it('logs out user without any side effect', async () => {
      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);

      expect(tokenService.getToken()).toBeUndefined();
    });

  });

});
