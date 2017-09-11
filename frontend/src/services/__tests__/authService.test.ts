import {AuthState} from '../../usecases/auth/authReducer';
import {initialAuthState, loadAuthState, makeToken, saveAuthState} from '../authService';
import {StorageService, storageService} from '../StorageService';
import {FakeStorage} from './FakeStorage';

describe('authService', () => {

  beforeEach(() => {
    const fakeTokenService = new StorageService(new FakeStorage());

    storageService.getItem = jest.fn((key) => {
      return fakeTokenService.getItem(key);
    });

    storageService.setItem = jest.fn((key: string, data: string) => {
      return fakeTokenService.setItem(key, data);
    });

    storageService.clear = jest.fn(() => {
      return fakeTokenService.clear();
    });
  });

  describe('make new basic encoded authorization token', () => {

    it('can create new token from empty username and password', () => {
      expect(makeToken('', '')).toEqual('Og==');
    });
  });

  describe('save auth state to local storage', () => {

    it('saves successful auth state', () => {
      const state: AuthState = {
        isAuthenticated: true,
        token: makeToken('user', 'password'),
        user: {
          firstName: 'clark',
          lastName: 'kent',
          email: 'a@b.com',
          company: 'daily planet',
          id: '3',
        },
      };

      saveAuthState(state);

      expect(loadAuthState()).toEqual(state);
    });

    it('does not save state when not authenticated', () => {
      const state: AuthState = {isAuthenticated: false};

      saveAuthState(state);

      expect(loadAuthState()).toEqual(initialAuthState);
    });

  });

  describe('user is logged out', () => {

    it('cannot load auth state', () => {
      storageService.clear();

      expect(loadAuthState()).toEqual(initialAuthState);
    });
  });
});
