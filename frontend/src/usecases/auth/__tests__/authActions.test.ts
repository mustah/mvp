import axios from 'axios';
import * as MockAdapter from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeToken} from '../../../services/authService';
import {routes} from '../../app/routes';
import {login, loginFailure, loginRequest, loginSuccess, logout, logoutRequest, logoutSuccess} from '../authActions';
import {UnauthorizedDTO} from '../authReducer';

const middlewares = [thunk];
const mockStore = configureStore(middlewares);

describe('authActions', () => {

  const user = {id: 1, firstName: 'clark', lastName: 'kent'};
  let mockRestClient;
  let store;
  let token: string;

  beforeEach(() => {
    store = mockStore({});
    mockRestClient = new MockAdapter(axios);
  });

  describe('authorized users', () => {

    const dispatchLogin = async () => {
      mockRestClient.onGet('/authenticate').reply(200, user);

      const username = 'the.batman@dc.com';
      const password = 'test1234';
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

      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token, user}),
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);
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
        path: '/api/authenticate',
      };
      mockRestClient.onGet('/authenticate').reply(unauthorized, errorMessage);

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
        path: '/api/authenticate',
      };

      mockRestClient.onGet('/authenticate').reply(internalServerError, errorMessage);

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
        path: '/api/authenticate',
      };
      mockRestClient.onGet('/authenticate').reply(internalServerError, errorMessage);

      await store.dispatch(login('foo', '123123'));
    });

    it('logs out user without any side effect', async () => {
      await store.dispatch(logout());

      expect(store.getActions()).toEqual([
        logoutRequest(),
        logoutSuccess(),
        routerActions.push(routes.home),
      ]);
    });
  });

});
