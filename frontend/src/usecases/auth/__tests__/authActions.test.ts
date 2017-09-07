import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {FakeStorage} from '../../../services/__tests__/FakeStorage';
import {tokenService, TokenService} from '../../../services/TokenService';
import {login, loginFailure, loginRequest, loginSuccess} from '../authActions';
import {UnauthorizedDTO} from '../authReducer';

describe('loginActions', () => {

  let middlewares;
  let mockStore;
  let initialState;
  let store;
  let mockRestClient;

  beforeEach(() => {
    middlewares = [thunk];
    mockStore = configureStore(middlewares);
    initialState = {};
    store = mockStore(initialState);
    mockRestClient = new MockAdapter(axios);

    const fakeTokenService = new TokenService(new FakeStorage());

    tokenService.getToken = jest.fn(() => {
      return fakeTokenService.getToken();
    });

    tokenService.setToken = jest.fn((token: string) => {
      return fakeTokenService.setToken(token);
    });
  });

  describe('authorized users', () => {

    it('dispatches the login action', async () => {
      const user = {id: 1, firstName: 'must'};
      mockRestClient.onGet('/users/1').reply(200, user);

      await store.dispatch(login('the.batman@dc.com', 'test1234'));

      expect(store.getActions()).toEqual([
        loginRequest(),
        loginSuccess({token: tokenService.getToken(), user}),
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

  });

});
